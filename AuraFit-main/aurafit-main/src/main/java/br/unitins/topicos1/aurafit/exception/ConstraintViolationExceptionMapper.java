package br.unitins.topicos1.aurafit.exception;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    UriInfo uriInfo;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(ConstraintViolationException e) {
        List<Problem.FieldError> fieldErrors = e.getConstraintViolations()
                .stream()
                .map(this::toFieldError)
                .toList();

        return ProblemFactory.build(baseUrl, "validation-error", "Erro de validação", Response.Status.BAD_REQUEST,
                "Um ou mais campos não passaram na validação.", uriInfo, fieldErrors);
    }

    private Problem.FieldError toFieldError(ConstraintViolation<?> v) {
        return new Problem.FieldError(caminhoDoCampo(v.getPropertyPath()), v.getMessage());
    }

    /**
     * Monta o caminho do campo ignorando o método e o parâmetro.
     * Ex.: create.arg0.endereco.cep -> "endereco.cep"; telefones[0].numero -> "telefones.0.numero".
     * Esse formato é o mesmo usado pelo form.get(...) do Angular.
     */
    private String caminhoDoCampo(Path path) {
        StringBuilder caminho = new StringBuilder();
        for (Path.Node node : path) {
            ElementKind kind = node.getKind();
            if (kind != ElementKind.PROPERTY && kind != ElementKind.CONTAINER_ELEMENT) {
                continue;
            }
            if (node.isInIterable() && node.getIndex() != null) {
                caminho.append(caminho.isEmpty() ? "" : ".").append(node.getIndex());
            }
            if (node.getName() != null && !node.getName().startsWith("<")) {
                caminho.append(caminho.isEmpty() ? "" : ".").append(node.getName());
            }
        }
        return caminho.isEmpty() ? "unknown" : caminho.toString();
    }
}
