package br.unitins.topicos1.fitwork.mapper;

import java.util.Comparator;

import br.unitins.topicos1.fitwork.dto.ProdutoResponseDTO;
import br.unitins.topicos1.fitwork.model.Produto;
import br.unitins.topicos1.fitwork.model.Sabor;

public final class ProdutoMapper {

    private ProdutoMapper() {
    }

    public static ProdutoResponseDTO toResponse(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getPesoGramas(),
                produto.getEstoque(),
                produto.getTipoProteina(),
                produto.isAtivo(),
                CategoriaMapper.toResponse(produto.getCategoria()),
                MarcaMapper.toResponse(produto.getMarca()),
                FornecedorMapper.toResponse(produto.getFornecedor()),
                produto.getSabores().stream()
                        .sorted(Comparator.comparing(Sabor::getNome))
                        .map(SaborMapper::toResponse)
                        .toList());
    }
}
