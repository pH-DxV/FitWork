package br.unitins.topicos1.fitwork.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "O e-mail deve ser informado.")
    @Email(message = "O e-mail informado é inválido.")
    @Length(max = 120, message = "O e-mail deve conter no máximo 120 caracteres.")
    String email,

    @NotBlank(message = "A senha deve ser informada.")
    @Length(min = 6, max = 64, message = "A senha deve conter entre 6 e 64 caracteres.")
    String senha
) {
}
