package br.unitins.topicos1.fitwork.mapper;

import br.unitins.topicos1.fitwork.dto.TelefoneRequestDTO;
import br.unitins.topicos1.fitwork.dto.TelefoneResponseDTO;
import br.unitins.topicos1.fitwork.model.Telefone;

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
