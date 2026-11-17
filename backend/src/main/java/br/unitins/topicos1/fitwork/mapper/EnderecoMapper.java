package br.unitins.topicos1.fitwork.mapper;

import br.unitins.topicos1.fitwork.dto.EnderecoResponseDTO;
import br.unitins.topicos1.fitwork.model.Endereco;

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
