package br.unitins.topicos1.fitwork.service;

import java.util.Set;

/**
 * Dados devolvidos pelo Keycloak após autenticação ou renovação de token.
 */
public record TokensKeycloak(
    String accessToken,
    String refreshToken,
    long expiresIn,
    long refreshExpiresIn,
    String tokenType,
    String keycloakId,
    String nome,
    String email,
    Set<String> perfis
) {
}
