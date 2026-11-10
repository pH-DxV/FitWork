package br.unitins.topicos1.aurafit.dto;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CNPJ;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MarcaRequestDTO(
    @NotBlank(message = "O nome da marca deve ser informado.")
    @Length(min = 2, max = 100, message = "O nome deve conter entre 2 e 100 caracteres.")
    String nome,

    @NotBlank(message = "O CNPJ deve ser informado.")
    @Pattern(regexp = "\\d{14}", message = "O CNPJ deve conter 14 números.")
    @CNPJ(message = "CNPJ inválido.")
    String cnpj,

    @Email(message = "O e-mail informado é inválido.")
    @Length(max = 100, message = "O e-mail deve conter no máximo 100 caracteres.")
    String emailContato,

    @NotNull(message = "O campo ativa deve ser informado.")
    Boolean ativa
) {
}
