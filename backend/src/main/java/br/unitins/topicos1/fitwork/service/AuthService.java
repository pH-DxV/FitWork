package br.unitins.topicos1.fitwork.service;

import br.unitins.topicos1.fitwork.dto.CadastroContaRequestDTO;
import br.unitins.topicos1.fitwork.dto.LoginRequestDTO;
import br.unitins.topicos1.fitwork.dto.UsuarioAutenticadoDTO;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface AuthService {

    SessaoTokens entrar(@NotNull(message = "As credenciais devem ser informadas.") @Valid LoginRequestDTO dto);

    SessaoTokens cadastrar(@NotNull(message = "Os dados do cadastro devem ser informados.") @Valid CadastroContaRequestDTO dto);

    SessaoTokens renovar(String refreshToken);

    void sair(String refreshToken);

    UsuarioAutenticadoDTO obterUsuarioAtual(SecurityIdentity identity);
}
