package br.unitins.topicos1.aurafit.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    @Context
    UriInfo uriInfo;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(JsonProcessingException e) {
        List<Problem.FieldError> erros = null;
        String detalhe = "JSON inválido ou malformado. Verifique a sintaxe e os tipos dos campos.";

        if (e instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String campo = extrairCaminho(ife.getPath());
            erros = List.of(new Problem.FieldError(campo, "Valor inválido para o tipo esperado."));
            detalhe = "Um ou mais campos possuem formato inválido.";
        } else if (e instanceof JsonMappingException jme && !jme.getPath().isEmpty()) {
            String campo = extrairCaminho(jme.getPath());
            erros = List.of(new Problem.FieldError(campo, "Não foi possível interpretar o valor informado."));
        }

        return ProblemFactory.build(
                baseUrl,
                "invalid-json",
                "Requisição malformada",
                Response.Status.BAD_REQUEST,
                detalhe,
                uriInfo,
                erros);
    }

    private String extrairCaminho(List<JsonMappingException.Reference> path) {
        return path.stream()
                .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : String.valueOf(ref.getIndex()))
                .collect(Collectors.joining("."));
    }
}
