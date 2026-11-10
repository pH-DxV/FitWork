package br.unitins.topicos1.aurafit.mapper;

import br.unitins.topicos1.aurafit.dto.FornecedorResponseDTO;
import br.unitins.topicos1.aurafit.model.Fornecedor;

public final class FornecedorMapper {

    private FornecedorMapper() {
    }

    public static FornecedorResponseDTO toResponse(Fornecedor fornecedor) {
        return new FornecedorResponseDTO(fornecedor.getId(), fornecedor.getRazaoSocial(), fornecedor.getCnpj(),
                fornecedor.getTelefone(), fornecedor.getEmail());
    }
}
