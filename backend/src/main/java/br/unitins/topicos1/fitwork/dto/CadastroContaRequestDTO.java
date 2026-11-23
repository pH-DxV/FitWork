package br.unitins.topicos1.fitwork.dto;

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

/**
 * Dados do autocadastro de cliente.
 * A senha é enviada exclusivamente ao Keycloak e NUNCA é gravada no banco do e-commerce.
 */
public record CadastroContaRequestDTO(
    @NotBlank(message = "O nome deve ser informado.")
    @Length(min = 2, max = 120, message = "O nome deve conter entre 2 e 120 caracteres.")
    String nome,

    @NotBlank(message = "O e-mail deve ser informado.")
    @Email(message = "O e-mail informado é inválido.")
    @Length(max = 120, message = "O e-mail deve conter no máximo 120 caracteres.")
    String email,

    @NotBlank(message = "A senha deve ser informada.")
    @Length(min = 8, max = 64, message = "A senha deve conter entre 8 e 64 caracteres.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "A senha deve conter ao menos uma letra maiúscula, uma minúscula e um número."
    )
    String senha,

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
    Boolean aceitaNovidades
) {
    /** Converte para o DTO cadastral (sem a senha). */
    public ClienteRequestDTO toClienteRequestDTO() {
        return new ClienteRequestDTO(
                nome,
                email,
                cpf,
                dataNascimento,
                genero,
                telefones,
                endereco,
                aceitaNovidades,
                true);
    }
}
