package br.unitins.topicos1.aurafit.mapper;

import br.unitins.topicos1.aurafit.dto.TelefoneRequestDTO;
import br.unitins.topicos1.aurafit.dto.TelefoneResponseDTO;
import br.unitins.topicos1.aurafit.model.Telefone;

public final class TelefoneMapper {

    private TelefoneMapper() {
    }

    public static Telefone toModel(TelefoneRequestDTO dto) {
        Telefone telefone = new Telefone();
        telefone.setCodigoArea(dto.codigoArea());
        telefone.setNumero(dto.numero());
        telefone.setWhatsapp(dto.whatsapp());
        return telefone;
    }

    public static TelefoneResponseDTO toResponse(Telefone telefone) {
        return new TelefoneResponseDTO(telefone.getCodigoArea(), telefone.getNumero(), telefone.isWhatsapp());
    }
}
