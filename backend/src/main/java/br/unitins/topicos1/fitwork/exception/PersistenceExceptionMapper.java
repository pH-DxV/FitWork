package br.unitins.topicos1.fitwork.exception;

import java.sql.SQLException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

    private static final Logger LOG = Logger.getLogger(PersistenceExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(PersistenceException e) {
        if (isConstraintViolation(e)) {
            return ProblemFactory.build(
                    baseUrl,
                    "conflict",
                    "Conflito de integridade",
                    Response.Status.CONFLICT,
                    "A operação viola uma restrição de unicidade ou vínculo no banco de dados.",
                    uriInfo,
                    null);
        }

        LOG.errorf(e, "Erro de persistência em %s", (uriInfo != null ? uriInfo.getRequestUri() : "<no-uri>"));
        return ProblemFactory.build(
                baseUrl,
                "unexpected-error",
                "Erro inesperado",
                Response.Status.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado ao persistir os dados.",
                uriInfo,
                null);
    }

    private boolean isConstraintViolation(Throwable t) {
        Throwable atual = t;
        while (atual != null) {
            if (atual instanceof ConstraintViolationException) {
                return true;
            }
            if (atual instanceof SQLException sqlEx && sqlEx.getSQLState() != null
                    && sqlEx.getSQLState().startsWith("23")) {
                return true;
            }
            atual = atual.getCause();
        }
        return false;
    }
}
