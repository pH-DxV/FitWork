package br.unitins.topicos1.aurafit.dto;

public record MunicipioResponseDTO(
    Long id,
    String nome,
    String codigoIbge,
    EstadoResponseDTO estado
) {
}
