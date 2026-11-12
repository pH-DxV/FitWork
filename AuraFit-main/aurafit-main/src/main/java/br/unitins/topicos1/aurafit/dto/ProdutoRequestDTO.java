package br.unitins.topicos1.aurafit.dto;

import java.math.BigDecimal;
import java.util.Set;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ProdutoRequestDTO(
    @NotBlank(message = "O nome do produto deve ser informado.")
    @Length(min = 2, max = 120, message = "O nome deve conter entre 2 e 120 caracteres.")
    @Pattern(regexp = "^\\s*\\S.{0,118}\\S\\s*$", message = "O nome deve conter entre 2 e 120 caracteres.")
    String nome,

    @Length(max = 1000, message = "A descrição deve conter no máximo 1000 caracteres.")
    String descricao,

    @NotNull(message = "O preço deve ser informado.")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
    @Digits(integer = 8, fraction = 2, message = "Use no máximo 2 casas decimais no preço.")
    BigDecimal preco,

    @NotNull(message = "O peso em gramas deve ser informado.")
    @Positive(message = "O peso deve ser maior que zero.")
    Integer pesoGramas,

    @NotNull(message = "O estoque deve ser informado.")
    @Min(value = 0, message = "O estoque não pode ser negativo.")
    Integer estoque,

    @NotNull(message = "O tipo de proteína deve ser informado.")
    Long idTipoProteina,

    @NotNull(message = "O campo ativo deve ser informado.")
    Boolean ativo,

    @NotNull(message = "A categoria deve ser informada.")
    Long idCategoria,

    @NotNull(message = "A marca deve ser informada.")
    Long idMarca,

    @NotNull(message = "O fornecedor deve ser informado.")
    Long idFornecedor,

    Set<Long> idsSabores
) {
}
