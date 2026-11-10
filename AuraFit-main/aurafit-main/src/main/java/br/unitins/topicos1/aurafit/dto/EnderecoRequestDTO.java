package br.unitins.topicos1.aurafit.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Endereço preenchido a partir do ViaCEP. Cidade, UF e código IBGE são usados
 * no back-end para localizar (ou cadastrar) o Município e o Estado.
 */
public record EnderecoRequestDTO(
    @NotBlank(message = "O CEP deve ser informado.")
    @Pattern(regexp = "\\d{8}", message = "O CEP deve conter 8 números.")
    String cep,

    @NotBlank(message = "O logradouro deve ser informado.")
    @Length(max = 120, message = "O logradouro deve conter no máximo 120 caracteres.")
    String logradouro,

    @NotBlank(message = "O número deve ser informado.")
    @Length(max = 10, message = "O número deve conter no máximo 10 caracteres.")
    String numero,

    @Length(max = 60, message = "O complemento deve conter no máximo 60 caracteres.")
    String complemento,

    @NotBlank(message = "O bairro deve ser informado.")
    @Length(max = 80, message = "O bairro deve conter no máximo 80 caracteres.")
    String bairro,

    @NotBlank(message = "A cidade deve ser informada.")
    @Length(max = 80, message = "A cidade deve conter no máximo 80 caracteres.")
    String cidade,

    @NotBlank(message = "A UF deve ser informada.")
    @Pattern(regexp = "[A-Za-z]{2}", message = "A UF deve conter 2 letras.")
    String uf,

    @Pattern(regexp = "\\d{7}", message = "O código IBGE deve conter 7 números.")
    String codigoIbge
) {
}
