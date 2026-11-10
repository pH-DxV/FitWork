package br.unitins.topicos1.aurafit.mapper;

import java.util.Comparator;

import br.unitins.topicos1.aurafit.dto.ProdutoResponseDTO;
import br.unitins.topicos1.aurafit.model.Produto;
import br.unitins.topicos1.aurafit.model.Sabor;

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
