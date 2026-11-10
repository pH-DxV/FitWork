package br.unitins.topicos1.aurafit.dto;

public record EnderecoResponseDTO(
    String cep,
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    MunicipioResponseDTO municipio
) {
}
