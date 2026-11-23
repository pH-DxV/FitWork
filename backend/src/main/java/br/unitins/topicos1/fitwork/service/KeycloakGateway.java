package br.unitins.topicos1.fitwork.service;

/**
 * Porta de comunicação do back-end Quarkus com o Keycloak (OAuth 2.0 / OIDC e Admin REST API).
 * O front-end Angular nunca acessa o Keycloak diretamente.
 */
public interface KeycloakGateway {

    TokensKeycloak autenticarComSenha(String email, String senha);

    TokensKeycloak renovarToken(String refreshToken);

    void encerrarSessao(String refreshToken);

    String criarUsuarioCliente(String nome, String email, String senha);

    void atualizarDadosUsuario(String keycloakId, String nome, String email, boolean ativo);

    void removerUsuario(String keycloakId);
}
