package br.unitins.topicos1.fitwork.mapper;

import br.unitins.topicos1.fitwork.dto.SaborResponseDTO;
import br.unitins.topicos1.fitwork.model.Sabor;

public final class SaborMapper {

    private SaborMapper() {
    }

    public static SaborResponseDTO toResponse(Sabor sabor) {
        return new SaborResponseDTO(sabor.getId(), sabor.getNome(), sabor.getDescricao());
    }
}
