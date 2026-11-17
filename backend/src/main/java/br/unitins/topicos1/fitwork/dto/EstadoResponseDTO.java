package br.unitins.topicos1.fitwork.dto;

import br.unitins.topicos1.fitwork.model.Regiao;

public record EstadoResponseDTO(
    Long id,
    String nome,
    String sigla,
    Regiao regiao
) {
}
