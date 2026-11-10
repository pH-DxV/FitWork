package br.unitins.topicos1.aurafit.mapper;

import br.unitins.topicos1.aurafit.dto.SaborResponseDTO;
import br.unitins.topicos1.aurafit.model.Sabor;

public final class SaborMapper {

    private SaborMapper() {
    }

    public static SaborResponseDTO toResponse(Sabor sabor) {
        return new SaborResponseDTO(sabor.getId(), sabor.getNome(), sabor.getDescricao());
    }
}
