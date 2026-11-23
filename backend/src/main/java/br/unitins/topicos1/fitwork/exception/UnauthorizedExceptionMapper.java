package br.unitins.topicos1.fitwork.exception;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

    @Context
    UriInfo uri;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(UnauthorizedException e) {
        String detalhe = (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage()
                : "É necessário estar autenticado para acessar este recurso.";
        return ProblemFactory.build(
                baseUrl,
                "unauthorized",
                "Não autenticado",
                Response.Status.UNAUTHORIZED,
                detalhe,
                uri,
                null);
    }
}
