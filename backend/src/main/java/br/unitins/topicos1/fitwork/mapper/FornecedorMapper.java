package br.unitins.topicos1.fitwork.mapper;

import br.unitins.topicos1.fitwork.dto.FornecedorResponseDTO;
import br.unitins.topicos1.fitwork.model.Fornecedor;

public final class FornecedorMapper {

    private FornecedorMapper() {
    }

    public static FornecedorResponseDTO toResponse(Fornecedor fornecedor) {
        return new FornecedorResponseDTO(fornecedor.getId(), fornecedor.getRazaoSocial(), fornecedor.getCnpj(),
                fornecedor.getTelefone(), fornecedor.getEmail());
    }
}
