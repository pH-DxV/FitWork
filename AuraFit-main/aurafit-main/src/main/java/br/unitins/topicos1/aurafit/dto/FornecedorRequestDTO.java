package br.unitins.topicos1.aurafit.dto;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CNPJ;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record FornecedorRequestDTO(
    @NotBlank(message = "A razão social deve ser informada.")
    @Length(min = 2, max = 120, message = "A razão social deve conter entre 2 e 120 caracteres.")
    String razaoSocial,

    @NotBlank(message = "O CNPJ deve ser informado.")
    @Pattern(regexp = "\\d{14}", message = "O CNPJ deve conter 14 números.")
    @CNPJ(message = "CNPJ inválido.")
    String cnpj,

    @NotBlank(message = "O telefone deve ser informado.")
    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter DDD e número (10 ou 11 números).")
    String telefone,

    @Email(message = "O e-mail informado é inválido.")
    @Length(max = 100, message = "O e-mail deve conter no máximo 100 caracteres.")
    String email
) {
}
