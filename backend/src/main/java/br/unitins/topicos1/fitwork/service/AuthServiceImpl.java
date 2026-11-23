package br.unitins.topicos1.fitwork.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.unitins.topicos1.fitwork.dto.AuthResponseDTO;
import br.unitins.topicos1.fitwork.dto.CadastroContaRequestDTO;
import br.unitins.topicos1.fitwork.dto.ClienteRequestDTO;
import br.unitins.topicos1.fitwork.dto.ClienteResponseDTO;
import br.unitins.topicos1.fitwork.dto.LoginRequestDTO;
import br.unitins.topicos1.fitwork.dto.UsuarioAutenticadoDTO;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

    private static final Set<String> PERFIS_APLICACAO = Set.of("ADMIN", "CLIENTE");

    @Inject
    KeycloakGateway keycloakGateway;

    @Inject
    ClienteService clienteService;

    @Override
    public SessaoTokens entrar(LoginRequestDTO dto) {
        TokensKeycloak tokens = keycloakGateway.autenticarComSenha(dto.email(), dto.senha());
        ClienteResponseDTO cliente = resolverClienteAtivoOuEncerrar(tokens);
        return montarSessao(tokens, cliente);
    }

    @Override
    public SessaoTokens cadastrar(CadastroContaRequestDTO dto) {
        ClienteRequestDTO dadosCliente = dto.toClienteRequestDTO();

        // 1. Valida regras de negócio e unicidade no PostgreSQL ANTES de criar o usuário no Keycloak
        clienteService.validarNovoCadastro(dadosCliente);

        // 2. Cria a conta no Keycloak com o papel CLIENTE (a senha fica somente no Keycloak)
        String keycloakId = keycloakGateway.criarUsuarioCliente(dto.nome(), dto.email(), dto.senha());

        // 3. Persiste Pessoa + Cliente vinculados ao keycloakId; se falhar, desfaz no Keycloak
        ClienteResponseDTO cliente;
        try {
            cliente = clienteService.criarVinculadoAoKeycloak(dadosCliente, keycloakId);
        } catch (RuntimeException e) {
            keycloakGateway.removerUsuario(keycloakId);
            throw e;
        }

        // 4. Autentica o cliente recém-cadastrado e devolve a sessão pronta
        TokensKeycloak tokens = keycloakGateway.autenticarComSenha(dto.email(), dto.senha());
        return montarSessao(tokens, cliente);
    }

    @Override
    public SessaoTokens renovar(String refreshToken) {
        TokensKeycloak tokens = keycloakGateway.renovarToken(refreshToken);
        ClienteResponseDTO cliente = resolverClienteAtivoOuEncerrar(tokens);
        return montarSessao(tokens, cliente);
    }

    @Override
    public void sair(String refreshToken) {
        keycloakGateway.encerrarSessao(refreshToken);
    }

    @Override
    public UsuarioAutenticadoDTO obterUsuarioAtual(SecurityIdentity identity) {
        if (identity == null || identity.isAnonymous()) {
            throw new UnauthorizedException("É necessário estar autenticado.");
        }

        String keycloakId = null;
        String email = null;
        String nome = identity.getPrincipal().getName();

        if (identity.getPrincipal() instanceof JsonWebToken jwt) {
            keycloakId = jwt.getSubject();
            email = jwt.getClaim("email");
            String claimNome = jwt.getClaim("name");
            if (claimNome != null && !claimNome.isBlank()) {
                nome = claimNome;
            }
            if (email == null || email.isBlank()) {
                email = jwt.getName();
            }
        }

        Set<String> perfis = identity.getRoles().stream()
                .filter(PERFIS_APLICACAO::contains)
                .collect(Collectors.toUnmodifiableSet());

        ClienteResponseDTO cliente = clienteService.vincularOuBuscarPorKeycloak(keycloakId, email);
        return new UsuarioAutenticadoDTO(
                keycloakId,
                cliente != null ? cliente.nome() : nome,
                cliente != null ? cliente.email() : email,
                perfis,
                cliente);
    }

    private ClienteResponseDTO resolverClienteAtivoOuEncerrar(TokensKeycloak tokens) {
        try {
            return clienteService.vincularOuBuscarPorKeycloak(tokens.keycloakId(), tokens.email());
        } catch (ForbiddenException e) {
            keycloakGateway.encerrarSessao(tokens.refreshToken());
            throw e;
        }
    }

    private SessaoTokens montarSessao(TokensKeycloak tokens, ClienteResponseDTO cliente) {
        UsuarioAutenticadoDTO usuario = new UsuarioAutenticadoDTO(
                tokens.keycloakId(),
                cliente != null ? cliente.nome() : tokens.nome(),
                cliente != null ? cliente.email() : tokens.email(),
                tokens.perfis(),
                cliente);

        AuthResponseDTO resposta = new AuthResponseDTO(
                tokens.accessToken(),
                tokens.tokenType(),
                tokens.expiresIn(),
                usuario);

        return new SessaoTokens(resposta, tokens.refreshToken(), tokens.refreshExpiresIn());
    }
}
