package br.unitins.topicos1.fitwork.mapper;

import br.unitins.topicos1.fitwork.dto.CategoriaResponseDTO;
import br.unitins.topicos1.fitwork.model.Categoria;

public final class CategoriaMapper {

    private CategoriaMapper() {
    }

    public static CategoriaResponseDTO toResponse(Categoria categoria) {
        return new CategoriaResponseDTO(categoria.getId(), categoria.getNome(), categoria.getDescricao());
    }
}
