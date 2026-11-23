package br.unitins.topicos1.fitwork.dto;

import java.util.Set;

public record UsuarioAutenticadoDTO(
    String keycloakId,
    String nome,
    String email,
    Set<String> perfis,
    ClienteResponseDTO cliente
) {
}
