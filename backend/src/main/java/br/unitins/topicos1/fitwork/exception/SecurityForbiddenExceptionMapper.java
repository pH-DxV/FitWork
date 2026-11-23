package br.unitins.topicos1.fitwork.exception;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class SecurityForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Context
    UriInfo uri;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(ForbiddenException e) {
        String detalhe = (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage()
                : "Você não possui permissão para realizar esta operação.";
        return ProblemFactory.build(
                baseUrl,
                "forbidden",
                "Acesso negado",
                Response.Status.FORBIDDEN,
                detalhe,
                uri,
                null);
    }
}
