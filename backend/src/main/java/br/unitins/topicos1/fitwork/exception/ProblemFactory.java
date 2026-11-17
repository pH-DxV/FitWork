package br.unitins.topicos1.fitwork.exception;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

final class ProblemFactory {

    private ProblemFactory() {
    }

    static Response build(String baseUrl, String tipo, String titulo, Response.Status status, String detalhe,
            UriInfo uri, List<Problem.FieldError> erros) {
        Problem p = new Problem();
        p.type = baseUrl + "/errors/" + tipo;
        p.title = titulo;
        p.status = status.getStatusCode();
        p.detail = detalhe;
        p.instance = (uri != null ? uri.getRequestUri().getPath() : null);
        p.timestamp = OffsetDateTime.now();
        p.errors = (erros == null || erros.isEmpty()) ? null : erros;

        return Response.status(status)
                .type("application/problem+json")
                .entity(p)
                .build();
    }
}
