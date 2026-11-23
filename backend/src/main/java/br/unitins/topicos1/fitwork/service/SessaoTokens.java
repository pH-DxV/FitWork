package br.unitins.topicos1.fitwork.service;

import br.unitins.topicos1.fitwork.dto.AuthResponseDTO;

/**
 * Agrupa o corpo público da resposta de autenticação (com o access token)
 * e o refresh token que será gravado apenas em cookie HttpOnly.
 */
public record SessaoTokens(
    AuthResponseDTO resposta,
    String refreshToken,
    long refreshExpiresIn
) {
}
