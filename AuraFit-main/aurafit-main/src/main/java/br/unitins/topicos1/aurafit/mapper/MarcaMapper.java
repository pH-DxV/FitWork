package br.unitins.topicos1.aurafit.mapper;

import br.unitins.topicos1.aurafit.dto.MarcaResponseDTO;
import br.unitins.topicos1.aurafit.model.Marca;

public final class MarcaMapper {

    private MarcaMapper() {
    }

    public static MarcaResponseDTO toResponse(Marca marca) {
        return new MarcaResponseDTO(marca.getId(), marca.getNome(), marca.getCnpj(), marca.getEmailContato(),
                marca.isAtiva());
    }
}
