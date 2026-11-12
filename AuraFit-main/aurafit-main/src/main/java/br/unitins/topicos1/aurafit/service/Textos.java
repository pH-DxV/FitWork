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

    /** Valida se o texto após trim() respeita o intervalo [min, max]. */
    public static void validarTamanhoUtil(String texto, String campo, int min, int max, String mensagem) {
        String limpo = limparOuNulo(texto);
        int tamanho = limpo == null ? 0 : limpo.length();
        if (tamanho < min || tamanho > max) {
            throw br.unitins.topicos1.aurafit.exception.ValidationException.of(campo, mensagem);
        }
    }
}
