package br.unitins.topicos1.aurafit.mapper;

import br.unitins.topicos1.aurafit.dto.EstadoResponseDTO;
import br.unitins.topicos1.aurafit.model.Estado;

public final class EstadoMapper {

    private EstadoMapper() {
    }

    public static EstadoResponseDTO toResponse(Estado estado) {
        return new EstadoResponseDTO(estado.getId(), estado.getNome(), estado.getSigla(), estado.getRegiao());
    }
}
