package br.unitins.topicos1.fitwork.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ClienteResponseDTO(
    Long id,
    String nome,
    String email,
    String cpf,
    LocalDate dataNascimento,
    String genero,
    List<TelefoneResponseDTO> telefones,
    EnderecoResponseDTO endereco,
    Boolean aceitaNovidades,
    Boolean ativo,
    LocalDateTime dataCadastro
) {
}
