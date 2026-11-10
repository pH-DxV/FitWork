package br.unitins.topicos1.aurafit.mapper;

import br.unitins.topicos1.aurafit.dto.EnderecoResponseDTO;
import br.unitins.topicos1.aurafit.model.Endereco;

public final class EnderecoMapper {

    private EnderecoMapper() {
    }

    public static EnderecoResponseDTO toResponse(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return new EnderecoResponseDTO(
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                MunicipioMapper.toResponse(endereco.getMunicipio()));
    }
}
