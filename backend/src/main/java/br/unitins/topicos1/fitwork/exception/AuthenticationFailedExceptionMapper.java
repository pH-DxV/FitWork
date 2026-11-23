package br.unitins.topicos1.fitwork.exception;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.security.AuthenticationFailedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class AuthenticationFailedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

    @Context
    UriInfo uri;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(AuthenticationFailedException e) {
        return ProblemFactory.build(
                baseUrl,
                "unauthorized",
                "Não autenticado",
                Response.Status.UNAUTHORIZED,
                "Sua sessão expirou ou o token de acesso é inválido. Faça login novamente.",
                uri,
                null);
    }
}
