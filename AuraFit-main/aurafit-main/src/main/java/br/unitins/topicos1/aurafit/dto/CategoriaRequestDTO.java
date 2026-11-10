package br.unitins.topicos1.aurafit.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequestDTO(
    @NotBlank(message = "O nome da categoria deve ser informado.")
    @Length(min = 2, max = 100, message = "O nome deve conter entre 2 e 100 caracteres.")
    String nome,

    @Length(max = 255, message = "A descrição deve conter no máximo 255 caracteres.")
    String descricao
) {
}
