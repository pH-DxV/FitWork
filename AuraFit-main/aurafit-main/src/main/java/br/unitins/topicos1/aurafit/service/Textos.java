package br.unitins.topicos1.aurafit.service;

public final class Textos {

    private Textos() {
    }

    /** Remove espaços extras e transforma texto vazio em null. */
    public static String limparOuNulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    /** Mantém apenas os dígitos (útil para CPF, CNPJ, CEP e telefone). */
    public static String somenteDigitos(String texto) {
        return texto == null ? null : texto.replaceAll("\\D", "");
    }
}
