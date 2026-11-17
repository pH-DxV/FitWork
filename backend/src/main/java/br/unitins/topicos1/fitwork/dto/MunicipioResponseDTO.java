package br.unitins.topicos1.fitwork.dto;

public record MunicipioResponseDTO(
    Long id,
    String nome,
    String codigoIbge,
    EstadoResponseDTO estado
) {
}
