package br.unitins.topicos1.fitwork.dto;

public record FornecedorResponseDTO(
    Long id,
    String razaoSocial,
    String cnpj,
    String telefone,
    String email
) {
}
