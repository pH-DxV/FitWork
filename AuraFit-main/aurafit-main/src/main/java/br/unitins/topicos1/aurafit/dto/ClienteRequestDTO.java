package br.unitins.topicos1.aurafit.dto;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

public record ClienteRequestDTO(
    @NotBlank(message = "O nome deve ser informado.")
    @Length(min = 2, max = 120, message = "O nome deve conter entre 2 e 120 caracteres.")
    String nome,

    @NotBlank(message = "O e-mail deve ser informado.")
    @Email(message = "O e-mail informado é inválido.")
    @Length(max = 120, message = "O e-mail deve conter no máximo 120 caracteres.")
    String email,

    @NotBlank(message = "O CPF deve ser informado.")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 números.")
    @CPF(message = "CPF inválido.")
    String cpf,

    @NotNull(message = "A data de nascimento deve ser informada.")
    @Past(message = "A data de nascimento deve ser anterior à data atual.")
    LocalDate dataNascimento,

    @Length(max = 30, message = "O gênero deve conter no máximo 30 caracteres.")
    String genero,

    @NotEmpty(message = "Ao menos um telefone deve ser informado.")
    List<@Valid TelefoneRequestDTO> telefones,

    @NotNull(message = "O endereço deve ser informado.")
    @Valid
    EnderecoRequestDTO endereco,

    @NotNull(message = "O campo aceitaNovidades deve ser informado.")
    Boolean aceitaNovidades,

    @NotNull(message = "O campo ativo deve ser informado.")
    Boolean ativo
) {
}
