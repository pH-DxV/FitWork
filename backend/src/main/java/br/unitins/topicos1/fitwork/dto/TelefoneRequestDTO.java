package br.unitins.topicos1.fitwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TelefoneRequestDTO(
    @NotBlank(message = "O DDD deve ser informado.")
    @Pattern(regexp = "\\d{2}", message = "O DDD deve conter 2 números.")
    String codigoArea,

    @NotBlank(message = "O número deve ser informado.")
    @Pattern(regexp = "\\d{8,9}", message = "O número deve conter 8 ou 9 dígitos.")
    String numero,

    @NotNull(message = "O campo whatsapp deve ser informado.")
    Boolean whatsapp
) {
}
