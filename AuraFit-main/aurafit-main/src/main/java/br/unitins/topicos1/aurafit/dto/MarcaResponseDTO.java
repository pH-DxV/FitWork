package br.unitins.topicos1.aurafit.dto;

public record MarcaResponseDTO(
    Long id,
    String nome,
    String cnpj,
    String emailContato,
    Boolean ativa
) {
}
