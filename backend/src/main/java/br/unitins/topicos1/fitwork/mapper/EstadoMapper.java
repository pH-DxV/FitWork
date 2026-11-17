package br.unitins.topicos1.fitwork.mapper;

import br.unitins.topicos1.fitwork.dto.EstadoResponseDTO;
import br.unitins.topicos1.fitwork.model.Estado;

public final class EstadoMapper {

    private EstadoMapper() {
    }

    public static EstadoResponseDTO toResponse(Estado estado) {
        return new EstadoResponseDTO(estado.getId(), estado.getNome(), estado.getSigla(), estado.getRegiao());
    }
}
