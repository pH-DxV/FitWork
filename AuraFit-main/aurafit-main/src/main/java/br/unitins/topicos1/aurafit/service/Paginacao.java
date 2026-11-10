package br.unitins.topicos1.aurafit.service;

import br.unitins.topicos1.aurafit.exception.ValidationException;

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
}
