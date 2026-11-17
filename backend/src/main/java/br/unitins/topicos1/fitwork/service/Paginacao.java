package br.unitins.topicos1.fitwork.service;

import java.util.function.Function;
import java.util.function.Supplier;

import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.exception.ValidationException;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

public final class Paginacao {

    public static final int TAMANHO_MAXIMO = 100;

    private Paginacao() {
    }

    public static void validar(int page, int pageSize) {
        if (page < 0) {
            throw ValidationException.of("page", "A página não pode ser negativa.");
        }
        if (pageSize < 1 || pageSize > TAMANHO_MAXIMO) {
            throw ValidationException.of("pageSize", "O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO + ".");
        }
    }

    public static <T, R> PageResponse<R> executar(Supplier<PanacheQuery<T>> consulta, int page, int pageSize,
            Function<T, R> mapper) {
        validar(page, pageSize);
        PanacheQuery<T> query = consulta.get();
        return PageResponse.of(query.page(page, pageSize).list(), page, pageSize, query.count(), mapper);
    }
}
