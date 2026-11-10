package br.unitins.topicos1.aurafit.mapper;

import br.unitins.topicos1.aurafit.dto.CategoriaResponseDTO;
import br.unitins.topicos1.aurafit.model.Categoria;

public final class CategoriaMapper {

    private CategoriaMapper() {
    }

    public static CategoriaResponseDTO toResponse(Categoria categoria) {
        return new CategoriaResponseDTO(categoria.getId(), categoria.getNome(), categoria.getDescricao());
    }
}
