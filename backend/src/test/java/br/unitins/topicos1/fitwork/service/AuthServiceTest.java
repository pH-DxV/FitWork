package br.unitins.topicos1.fitwork.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unitins.topicos1.fitwork.dto.CadastroContaRequestDTO;
import br.unitins.topicos1.fitwork.dto.ClienteRequestDTO;
import br.unitins.topicos1.fitwork.dto.ClienteResponseDTO;
import br.unitins.topicos1.fitwork.dto.EnderecoRequestDTO;
import br.unitins.topicos1.fitwork.dto.LoginRequestDTO;
import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.dto.TelefoneRequestDTO;
import br.unitins.topicos1.fitwork.exception.ValidationException;
import io.quarkus.security.ForbiddenException;

class AuthServiceTest {

    private final AuthServiceImpl authService = new AuthServiceImpl();

    private boolean validouPreviamente;
    private boolean criouNoKeycloak;
    private String usuarioRemovidoNoKeycloak;
    private String refreshTokenEncerrado;
    private boolean falharPersistenciaBanco;
    private boolean clienteDesativado;

    @BeforeEach
    void preparar() {
        validouPreviamente = false;
        criouNoKeycloak = false;
        usuarioRemovidoNoKeycloak = null;
        refreshTokenEncerrado = null;
        falharPersistenciaBanco = false;
        clienteDesativado = false;

        authService.keycloakGateway = new KeycloakGateway() {
            @Override
            public TokensKeycloak autenticarComSenha(String email, String senha) {
                return new TokensKeycloak(
                        "jwt-access-token",
                        "jwt-refresh-token",
                        300,
                        1800,
                        "Bearer",
                        "kc-sub-123",
                        "Ana Martins",
                        email,
                        Set.of("CLIENTE"));
            }

            @Override
            public TokensKeycloak renovarToken(String refreshToken) {
                return autenticarComSenha("ana.martins@fitwork.com", "ignorado");
            }

            @Override
            public void encerrarSessao(String refreshToken) {
                refreshTokenEncerrado = refreshToken;
            }

            @Override
            public String criarUsuarioCliente(String nome, String email, String senha) {
                criouNoKeycloak = true;
                return "kc-sub-novo";
            }

            @Override
            public void atualizarDadosUsuario(String keycloakId, String nome, String email, boolean ativo) {
            }

            @Override
            public void removerUsuario(String keycloakId) {
                usuarioRemovidoNoKeycloak = keycloakId;
            }
        };

        authService.clienteService = new ClienteService() {
            @Override
            public PageResponse<ClienteResponseDTO> findAll(int page, int pageSize) {
                return null;
            }

            @Override
            public PageResponse<ClienteResponseDTO> findByNome(String nome, int page, int pageSize) {
                return null;
            }

            @Override
            public ClienteResponseDTO findById(long id) {
                return null;
            }

            @Override
            public ClienteResponseDTO findByCpf(String cpf) {
                return null;
            }

            @Override
            public ClienteResponseDTO create(ClienteRequestDTO dto) {
                return null;
            }

            @Override
            public ClienteResponseDTO update(long id, ClienteRequestDTO dto) {
                return null;
            }

            @Override
            public void delete(long id) {
            }

            @Override
            public void validarNovoCadastro(ClienteRequestDTO dto) {
                validouPreviamente = true;
                if ("duplicado@fitwork.com".equalsIgnoreCase(dto.email())) {
                    throw ValidationException.conflict("email", "Já existe uma pessoa cadastrada com esse e-mail.");
                }
            }

            @Override
            public ClienteResponseDTO criarVinculadoAoKeycloak(ClienteRequestDTO dto, String keycloakId) {
                if (falharPersistenciaBanco) {
                    throw new IllegalStateException("Falha simulada ao gravar no banco.");
                }
                return clienteResposta(dto.nome(), dto.email());
            }

            @Override
            public ClienteResponseDTO vincularOuBuscarPorKeycloak(String keycloakId, String email) {
                if (clienteDesativado) {
                    throw new ForbiddenException("Sua conta de cliente está desativada.");
                }
                return clienteResposta("Ana Martins", email);
            }
        };
    }

    @Test
    void deveAutenticarEVincularCliente() {
        SessaoTokens sessao = authService.entrar(new LoginRequestDTO("ana.martins@fitwork.com", "Cliente@123"));

        assertEquals("jwt-access-token", sessao.resposta().accessToken());
        assertEquals("jwt-refresh-token", sessao.refreshToken());
        assertTrue(sessao.resposta().usuario().perfis().contains("CLIENTE"));
        assertNotNull(sessao.resposta().usuario().cliente());
    }

    @Test
    void deveRevogarSessaoNoKeycloakQuandoClienteEstiverInativoNoBanco() {
        clienteDesativado = true;

        assertThrows(ForbiddenException.class,
                () -> authService.entrar(new LoginRequestDTO("ana.martins@fitwork.com", "Cliente@123")));
        assertEquals("jwt-refresh-token", refreshTokenEncerrado);
    }

    @Test
    void naoDeveChamarKeycloakSeValidacaoPreviaDoBancoFalhar() {
        CadastroContaRequestDTO dto = dtoCadastro("duplicado@fitwork.com");

        assertThrows(ValidationException.class, () -> authService.cadastrar(dto));
        assertTrue(validouPreviamente);
        assertFalse(criouNoKeycloak);
        assertNull(usuarioRemovidoNoKeycloak);
    }

    @Test
    void deveDesfazerUsuarioNoKeycloakSePersistenciaNoBancoFalhar() {
        falharPersistenciaBanco = true;
        CadastroContaRequestDTO dto = dtoCadastro("novo@fitwork.com");

        assertThrows(IllegalStateException.class, () -> authService.cadastrar(dto));
        assertTrue(criouNoKeycloak);
        assertEquals("kc-sub-novo", usuarioRemovidoNoKeycloak);
    }

    private static CadastroContaRequestDTO dtoCadastro(String email) {
        return new CadastroContaRequestDTO(
                "Carlos Silva",
                email,
                "SenhaForte@123",
                "52998224725",
                LocalDate.of(1995, 5, 10),
                "Masculino",
                List.of(new TelefoneRequestDTO("63", "999990000", true)),
                new EnderecoRequestDTO(
                        "77001002",
                        "Quadra 104 Norte",
                        "12",
                        null,
                        "Plano Diretor Norte",
                        "Palmas",
                        "TO",
                        "1721000"),
                true);
    }

    private static ClienteResponseDTO clienteResposta(String nome, String email) {
        return new ClienteResponseDTO(
                1L,
                nome,
                email,
                "52998224725",
                LocalDate.of(1994, 3, 12),
                "Feminino",
                List.of(),
                null,
                true,
                true,
                LocalDateTime.now());
    }
}
