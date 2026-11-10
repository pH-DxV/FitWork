package br.unitins.topicos1.aurafit.dto;

import java.math.BigDecimal;
import java.util.List;

import br.unitins.topicos1.aurafit.model.TipoProteina;

public record ProdutoResponseDTO(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco,
    Integer pesoGramas,
    Integer estoque,
    TipoProteina tipoProteina,
    Boolean ativo,
    CategoriaResponseDTO categoria,
    MarcaResponseDTO marca,
    FornecedorResponseDTO fornecedor,
    List<SaborResponseDTO> sabores
) {
}
