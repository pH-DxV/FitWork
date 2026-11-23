package br.unitins.topicos1.fitwork.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.unitins.topicos1.fitwork.exception.ValidationException;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ServiceUnavailableException;

/**
 * Implementação HTTP da integração com o Keycloak.
 * Mantém o client_secret restrito ao servidor Quarkus e executa:
 * - OAuth 2.0 / OIDC (token, refresh, logout);
 * - Admin REST API via Service Account do client confidencial (criação de conta e atribuição da role CLIENTE).
 */
@ApplicationScoped
public class KeycloakHttpGateway implements KeycloakGateway {

    private static final Logger LOG = Logger.getLogger(KeycloakHttpGateway.class);
    private static final String PERFIL_CLIENTE = "CLIENTE";
    private static final Set<String> PERFIS_APLICACAO = Set.of("ADMIN", "CLIENTE");
    private static final Duration TIMEOUT = Duration.ofSeconds(8);

    @ConfigProperty(name = "fitwork.keycloak.url")
    String keycloakUrl;

    @ConfigProperty(name = "fitwork.keycloak.realm")
    String realm;

    @ConfigProperty(name = "fitwork.keycloak.client-id")
    String clientId;

    @ConfigProperty(name = "fitwork.keycloak.client-secret")
    String clientSecret;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    @Override
    public TokensKeycloak autenticarComSenha(String email, String senha) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "password");
        form.put("client_id", clientId);
        form.put("client_secret", clientSecret);
        form.put("scope", "openid profile email");
        form.put("username", email.trim().toLowerCase());
        form.put("password", senha);

        HttpResponse<String> response = enviarForm(urlToken(), form);
        if (response.statusCode() == 400 || response.statusCode() == 401) {
            // Mensagem genérica para não revelar se o e-mail existe ou não (prevenção de enumeração de contas)
            throw new UnauthorizedException("E-mail ou senha inválidos.");
        }
        if (response.statusCode() >= 400) {
            LOG.errorf("Falha ao autenticar no Keycloak (HTTP %d).", response.statusCode());
            throw new ServiceUnavailableException("Serviço de autenticação temporariamente indisponível.");
        }
        return extrairTokens(response.body());
    }

    @Override
    public TokensKeycloak renovarToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Sessão expirada. Faça login novamente.");
        }

        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "refresh_token");
        form.put("client_id", clientId);
        form.put("client_secret", clientSecret);
        form.put("refresh_token", refreshToken);

        HttpResponse<String> response = enviarForm(urlToken(), form);
        if (response.statusCode() == 400 || response.statusCode() == 401) {
            throw new UnauthorizedException("Sessão expirada. Faça login novamente.");
        }
        if (response.statusCode() >= 400) {
            LOG.errorf("Falha ao renovar token no Keycloak (HTTP %d).", response.statusCode());
            throw new ServiceUnavailableException("Serviço de autenticação temporariamente indisponível.");
        }
        return extrairTokens(response.body());
    }

    @Override
    public void encerrarSessao(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        Map<String, String> form = new LinkedHashMap<>();
        form.put("client_id", clientId);
        form.put("client_secret", clientSecret);
        form.put("refresh_token", refreshToken);

        try {
            HttpResponse<String> response = enviarForm(urlLogout(), form);
            if (response.statusCode() >= 400) {
                LOG.warnf("Keycloak retornou HTTP %d ao encerrar sessão.", response.statusCode());
            }
        } catch (RuntimeException e) {
            LOG.warn("Não foi possível notificar o Keycloak sobre o encerramento da sessão.", e);
        }
    }

    @Override
    public String criarUsuarioCliente(String nome, String email, String senha) {
        String tokenAdmin = obterTokenServiceAccount();
        String emailNormalizado = email.trim().toLowerCase();
        String[] partesNome = dividirNome(nome);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", emailNormalizado);
        payload.put("email", emailNormalizado);
        payload.put("firstName", partesNome[0]);
        payload.put("lastName", partesNome[1]);
        payload.put("enabled", true);
        payload.put("emailVerified", true);
        payload.put("credentials", List.of(Map.of(
                "type", "password",
                "value", senha,
                "temporary", false)));

        HttpResponse<String> criacao = enviarJson("POST", urlAdminUsuarios(), tokenAdmin, payload);
        if (criacao.statusCode() == 409) {
            throw ValidationException.conflict("email", "Já existe uma conta cadastrada com esse e-mail.");
        }
        if (criacao.statusCode() == 400) {
            throw ValidationException.of("senha",
                    "A senha não atende aos critérios de segurança (mínimo de 8 caracteres, maiúscula, minúscula e número).");
        }
        if (criacao.statusCode() != 201) {
            LOG.errorf("Erro ao criar usuário no Keycloak (HTTP %d): %s", criacao.statusCode(), criacao.body());
            throw new ServiceUnavailableException("Não foi possível registrar a conta no provedor de identidade.");
        }

        String keycloakId = extrairIdDoLocation(criacao)
                .orElseGet(() -> buscarIdPorEmail(tokenAdmin, emailNormalizado));

        try {
            atribuirPerfilCliente(tokenAdmin, keycloakId);
        } catch (RuntimeException e) {
            removerUsuarioComToken(tokenAdmin, keycloakId);
            throw e;
        }
        return keycloakId;
    }

    @Override
    public void atualizarDadosUsuario(String keycloakId, String nome, String email, boolean ativo) {
        if (keycloakId == null || keycloakId.isBlank()) {
            return;
        }
        try {
            String tokenAdmin = obterTokenServiceAccount();
            String[] partesNome = dividirNome(nome);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("email", email.trim().toLowerCase());
            payload.put("firstName", partesNome[0]);
            payload.put("lastName", partesNome[1]);
            payload.put("enabled", ativo);

            HttpResponse<String> resp = enviarJson("PUT", urlAdminUsuarios() + "/" + encodePath(keycloakId),
                    tokenAdmin, payload);
            if (resp.statusCode() >= 400) {
                LOG.warnf("Não foi possível sincronizar usuário %s no Keycloak (HTTP %d).",
                        keycloakId, resp.statusCode());
            }
        } catch (RuntimeException e) {
            LOG.warnf(e, "Falha ao sincronizar alterações do usuário %s no Keycloak.", keycloakId);
        }
    }

    @Override
    public void removerUsuario(String keycloakId) {
        if (keycloakId == null || keycloakId.isBlank()) {
            return;
        }
        try {
            String tokenAdmin = obterTokenServiceAccount();
            removerUsuarioComToken(tokenAdmin, keycloakId);
        } catch (RuntimeException e) {
            LOG.errorf(e, "Falha ao remover usuário %s no Keycloak durante compensação.", keycloakId);
        }
    }

    // ---------- métodos auxiliares privados ----------

    private void removerUsuarioComToken(String tokenAdmin, String keycloakId) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(urlAdminUsuarios() + "/" + encodePath(keycloakId)))
                .timeout(TIMEOUT)
                .header("Authorization", "Bearer " + tokenAdmin)
                .DELETE()
                .build();
        executar(request);
    }

    private void atribuirPerfilCliente(String tokenAdmin, String keycloakId) {
        HttpRequest buscarRole = HttpRequest.newBuilder(
                URI.create(urlBaseAdmin() + "/roles/" + encodePath(PERFIL_CLIENTE)))
                .timeout(TIMEOUT)
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> roleResp = executar(buscarRole);
        if (roleResp.statusCode() != 200) {
            LOG.errorf("Role %s não encontrada no Keycloak (HTTP %d).", PERFIL_CLIENTE, roleResp.statusCode());
            throw new ServiceUnavailableException("Configuração de perfil de acesso indisponível.");
        }

        JsonNode roleJson = lerJson(roleResp.body());
        List<Map<String, Object>> roles = List.of(Map.of(
                "id", roleJson.path("id").asText(),
                "name", roleJson.path("name").asText(PERFIL_CLIENTE)));

        String urlMapeamento = urlAdminUsuarios() + "/" + encodePath(keycloakId) + "/role-mappings/realm";
        HttpResponse<String> mapResp = enviarJson("POST", urlMapeamento, tokenAdmin, roles);
        if (mapResp.statusCode() != 204 && mapResp.statusCode() != 200) {
            LOG.errorf("Falha ao atribuir role CLIENTE ao usuário %s (HTTP %d).", keycloakId, mapResp.statusCode());
            throw new ServiceUnavailableException("Não foi possível atribuir o perfil de cliente à conta.");
        }
    }

    private String obterTokenServiceAccount() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "client_credentials");
        form.put("client_id", clientId);
        form.put("client_secret", clientSecret);

        HttpResponse<String> response = enviarForm(urlToken(), form);
        if (response.statusCode() != 200) {
            LOG.errorf("Falha ao obter token da Service Account no Keycloak (HTTP %d).", response.statusCode());
            throw new ServiceUnavailableException("Serviço de identidade indisponível no momento.");
        }
        JsonNode json = lerJson(response.body());
        return json.path("access_token").asText();
    }

    private java.util.Optional<String> extrairIdDoLocation(HttpResponse<?> response) {
        return response.headers().firstValue("Location")
                .or(() -> response.headers().firstValue("location"))
                .map(loc -> {
                    int barra = loc.lastIndexOf('/');
                    return barra >= 0 && barra + 1 < loc.length() ? loc.substring(barra + 1) : null;
                });
    }

    private String buscarIdPorEmail(String tokenAdmin, String email) {
        String url = urlAdminUsuarios() + "?username=" + encodeQuery(email) + "&exact=true";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .header("Authorization", "Bearer " + tokenAdmin)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> resp = executar(req);
        JsonNode arr = lerJson(resp.body());
        if (arr.isArray() && !arr.isEmpty()) {
            return arr.get(0).path("id").asText();
        }
        throw new ServiceUnavailableException("Conta criada, mas não foi possível localizar seu identificador.");
    }

    private TokensKeycloak extrairTokens(String body) {
        JsonNode json = lerJson(body);
        String accessToken = json.path("access_token").asText();
        String refreshToken = json.path("refresh_token").asText(null);
        long expiresIn = json.path("expires_in").asLong(300);
        long refreshExpiresIn = json.path("refresh_expires_in").asLong(1800);
        String tokenType = json.path("token_type").asText("Bearer");

        JsonNode claims = decodificarPayloadJwt(accessToken);
        String sub = claims.path("sub").asText();
        String email = claims.path("email").asText(claims.path("preferred_username").asText(""));
        String nome = claims.path("name").asText(email);

        Set<String> perfis = new LinkedHashSet<>();
        JsonNode rolesNode = claims.path("realm_access").path("roles");
        if (rolesNode.isArray()) {
            for (JsonNode r : rolesNode) {
                String role = r.asText();
                if (PERFIS_APLICACAO.contains(role)) {
                    perfis.add(role);
                }
            }
        }

        return new TokensKeycloak(
                accessToken,
                refreshToken,
                expiresIn,
                refreshExpiresIn,
                tokenType,
                sub,
                nome,
                email,
                 Set.copyOf(perfis));
    }

    private JsonNode decodificarPayloadJwt(String jwt) {
        String[] partes = jwt.split("\\.");
        if (partes.length < 2) {
            throw new UnauthorizedException("Token de acesso malformado.");
        }
        byte[] decoded = Base64.getUrlDecoder().decode(partes[1]);
        return lerJson(new String(decoded, StandardCharsets.UTF_8));
    }

    private HttpResponse<String> enviarForm(String url, Map<String, String> form) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(encodeQuery(entry.getKey())).append('=').append(encodeQuery(entry.getValue()));
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(sb.toString()))
                .build();
        return executar(request);
    }

    private HttpResponse<String> enviarJson(String metodo, String url, String bearerToken, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(TIMEOUT)
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .method(metodo, HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return executar(request);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao serializar requisição para o Keycloak.", e);
        }
    }

    private HttpResponse<String> executar(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("Comunicação com o provedor de identidade interrompida.");
        } catch (IOException e) {
            LOG.errorf(e, "Erro de rede ao comunicar com o Keycloak em %s", keycloakUrl);
            throw new ServiceUnavailableException(
                    "Não foi possível conectar ao servidor de identidade (Keycloak). Verifique se o container está em execução.");
        }
    }

    private JsonNode lerJson(String conteudo) {
        try {
            return objectMapper.readTree(conteudo);
        } catch (IOException e) {
            throw new IllegalStateException("Resposta JSON inválida recebida do Keycloak.", e);
        }
    }

    private String[] dividirNome(String nomeCompleto) {
        String limpo = nomeCompleto == null ? "" : nomeCompleto.trim();
        int espaco = limpo.indexOf(' ');
        if (espaco < 0) {
            return new String[] { limpo, "FitWork" };
        }
        return new String[] { limpo.substring(0, espaco).trim(), limpo.substring(espaco + 1).trim() };
    }

    private String urlToken() {
        return normalizarBase(keycloakUrl) + "/realms/" + encodePath(realm) + "/protocol/openid-connect/token";
    }

    private String urlLogout() {
        return normalizarBase(keycloakUrl) + "/realms/" + encodePath(realm) + "/protocol/openid-connect/logout";
    }

    private String urlBaseAdmin() {
        return normalizarBase(keycloakUrl) + "/admin/realms/" + encodePath(realm);
    }

    private String urlAdminUsuarios() {
        return urlBaseAdmin() + "/users";
    }

    private static String normalizarBase(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String encodeQuery(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8);
    }

    private static String encodePath(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
