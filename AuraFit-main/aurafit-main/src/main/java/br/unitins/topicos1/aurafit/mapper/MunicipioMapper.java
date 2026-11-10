package br.unitins.topicos1.aurafit.mapper;

import br.unitins.topicos1.aurafit.dto.MunicipioResponseDTO;
import br.unitins.topicos1.aurafit.model.Municipio;

public final class MunicipioMapper {

    private MunicipioMapper() {
    }

    public static MunicipioResponseDTO toResponse(Municipio municipio) {
        return new MunicipioResponseDTO(municipio.getId(), municipio.getNome(), municipio.getCodigoIbge(),
                EstadoMapper.toResponse(municipio.getEstado()));
    }
}
