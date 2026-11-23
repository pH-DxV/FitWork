package br.unitins.topicos1.fitwork.resource;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.unitins.topicos1.fitwork.dto.CadastroContaRequestDTO;
import br.unitins.topicos1.fitwork.dto.LoginRequestDTO;
import br.unitins.topicos1.fitwork.dto.UsuarioAutenticadoDTO;
import br.unitins.topicos1.fitwork.service.AuthService;
import br.unitins.topicos1.fitwork.service.SessaoTokens;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

/**
 * Endpoints de autenticação e sessão intermediados pelo back-end Quarkus.
 * O Angular comunica-se exclusivamente com este resource; o refresh token é mantido
 * em cookie HttpOnly + SameSite=Strict (inacessível a scripts no navegador).
 */
@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final String COOKIE_REFRESH = "FITWORK_RT";

    @Inject
    AuthService authService;

    @Inject
    SecurityIdentity securityIdentity;

    @ConfigProperty(name = "fitwork.auth.cookie-path", defaultValue = "/auth")
    String cookiePath;

    @ConfigProperty(name = "fitwork.auth.cookie-secure", defaultValue = "false")
    boolean cookieSecure;

    @POST
    @Path("/login")
    @PermitAll
    public Response entrar(
            @NotNull(message = "As credenciais devem ser informadas.") @Valid LoginRequestDTO dto) {
        SessaoTokens sessao = authService.entrar(dto);
        return responderComSessao(Response.Status.OK, sessao);
    }

    @POST
    @Path("/cadastro")
    @PermitAll
    public Response cadastrar(
            @NotNull(message = "Os dados do cadastro devem ser informados.") @Valid CadastroContaRequestDTO dto) {
        SessaoTokens sessao = authService.cadastrar(dto);
        return responderComSessao(Response.Status.CREATED, sessao);
    }

    @POST
    @Path("/refresh")
    @PermitAll
    @Consumes(MediaType.WILDCARD)
    public Response renovar(@CookieParam(COOKIE_REFRESH) Cookie cookieRefresh) {
        String refreshToken = cookieRefresh != null ? cookieRefresh.getValue() : null;
        SessaoTokens sessao = authService.renovar(refreshToken);
        return responderComSessao(Response.Status.OK, sessao);
    }

    @POST
    @Path("/logout")
    @PermitAll
    @Consumes(MediaType.WILDCARD)
    public Response sair(@CookieParam(COOKIE_REFRESH) Cookie cookieRefresh) {
        String refreshToken = cookieRefresh != null ? cookieRefresh.getValue() : null;
        authService.sair(refreshToken);
        return Response.noContent()
                .cookie(criarCookieRefresh("", 0))
                .header("Cache-Control", "no-store")
                .header("Pragma", "no-cache")
                .build();
    }

    @GET
    @Path("/me")
    @Authenticated
    public UsuarioAutenticadoDTO me() {
        return authService.obterUsuarioAtual(securityIdentity);
    }

    private Response responderComSessao(Response.Status status, SessaoTokens sessao) {
        return Response.status(status)
                .cookie(criarCookieRefresh(sessao.refreshToken(), sessao.refreshExpiresIn()))
                .header("Cache-Control", "no-store")
                .header("Pragma", "no-cache")
                .entity(sessao.resposta())
                .build();
    }

    private NewCookie criarCookieRefresh(String valor, long maxAgeSegundos) {
        return new NewCookie.Builder(COOKIE_REFRESH)
                .value(valor != null ? valor : "")
                .path(cookiePath)
                .maxAge((int) Math.max(0, maxAgeSegundos))
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }
}
