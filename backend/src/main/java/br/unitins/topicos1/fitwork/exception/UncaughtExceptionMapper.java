package br.unitins.topicos1.fitwork.exception;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class UncaughtExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(UncaughtExceptionMapper.class);

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable t) {
        if (t instanceof jakarta.ws.rs.ServiceUnavailableException sue) {
            return ProblemFactory.build(baseUrl, "service-unavailable", "Serviço indisponível",
                    Response.Status.SERVICE_UNAVAILABLE,
                    sue.getMessage() != null ? sue.getMessage() : "O serviço solicitado está temporariamente indisponível.",
                    uriInfo, null);
        }

        // erros HTTP "conhecidos" (ex.: 400 de JSON malformado, 405) mantêm o status original
        if (t instanceof WebApplicationException wae && wae.getResponse().getStatus() < 500) {
            Response.Status status = Response.Status.fromStatusCode(wae.getResponse().getStatus());
            return ProblemFactory.build(baseUrl, "http-error",
                    status != null ? status.getReasonPhrase() : "Erro na requisição",
                    status != null ? status : Response.Status.BAD_REQUEST,
                    "A requisição não pôde ser processada. Verifique os dados enviados.", uriInfo, null);
        }

        LOG.errorf(t, "Erro inesperado em %s", (uriInfo != null ? uriInfo.getRequestUri() : "<no-uri>"));

        return ProblemFactory.build(baseUrl, "unexpected-error", "Erro inesperado",
                Response.Status.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                uriInfo, null);
    }
}
