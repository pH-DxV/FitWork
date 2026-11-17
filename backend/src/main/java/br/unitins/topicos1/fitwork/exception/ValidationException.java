package br.unitins.topicos1.fitwork.exception;

import java.util.List;

import jakarta.ws.rs.core.Response;

public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final List<Problem.FieldError> fieldErrors;
    private final Response.Status status;

    public ValidationException(String msg, List<Problem.FieldError> errors) {
        this(msg, errors, Response.Status.BAD_REQUEST);
    }

    public ValidationException(String msg, List<Problem.FieldError> errors, Response.Status status) {
        super(msg);
        this.fieldErrors = (errors == null) ? List.of() : List.copyOf(errors);
        this.status = status != null ? status : Response.Status.BAD_REQUEST;
    }

    public static ValidationException of(String field, String msg) {
        return new ValidationException(msg, List.of(new Problem.FieldError(field, msg)), Response.Status.BAD_REQUEST);
    }

    public static ValidationException conflict(String field, String msg) {
        return new ValidationException(msg, List.of(new Problem.FieldError(field, msg)), Response.Status.CONFLICT);
    }

    public List<Problem.FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public Response.Status getStatus() {
        return status;
    }
}
