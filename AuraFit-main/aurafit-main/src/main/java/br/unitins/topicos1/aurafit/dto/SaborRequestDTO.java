package br.unitins.topicos1.aurafit.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SaborRequestDTO(
    @NotBlank(message = "O nome do sabor deve ser informado.")
    @Length(min = 2, max = 80, message = "O nome deve conter entre 2 e 80 caracteres.")
    @Pattern(regexp = "^\\s*\\S.{0,78}\\S\\s*$", message = "O nome deve conter entre 2 e 80 caracteres.")
    String nome,

    @Length(max = 255, message = "A descrição deve conter no máximo 255 caracteres.")
    String descricao
) {
}
