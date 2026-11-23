package br.unitins.topicos1.fitwork.dto;

/**
 * Resposta de autenticação enviada ao Angular.
 * Por segurança, o refresh token NÃO trafega neste JSON: ele fica somente em cookie HttpOnly.
 */
public record AuthResponseDTO(
    String accessToken,
    String tokenType,
    long expiresIn,
    UsuarioAutenticadoDTO usuario
) {
}
