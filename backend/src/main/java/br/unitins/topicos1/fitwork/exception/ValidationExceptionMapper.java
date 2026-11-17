package br.unitins.topicos1.fitwork.exception;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Context
    UriInfo uri;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(ValidationException e) {
        Response.Status status = e.getStatus();
        boolean conflito = status == Response.Status.CONFLICT;
        return ProblemFactory.build(
                baseUrl,
                conflito ? "conflict" : "validation-error",
                conflito ? "Conflito de dados" : "Erro de validação",
                status,
                e.getMessage(),
                uri,
                e.getFieldErrors());
    }
}
