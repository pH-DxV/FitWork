package br.unitins.topicos1.aurafit.dto;

import br.unitins.topicos1.aurafit.model.Regiao;

public record EstadoResponseDTO(
    Long id,
    String nome,
    String sigla,
    Regiao regiao
) {
}
