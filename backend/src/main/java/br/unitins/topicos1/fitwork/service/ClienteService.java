package br.unitins.topicos1.fitwork.service;

import br.unitins.topicos1.fitwork.dto.ClienteRequestDTO;
import br.unitins.topicos1.fitwork.dto.ClienteResponseDTO;
import br.unitins.topicos1.fitwork.dto.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface ClienteService {

    PageResponse<ClienteResponseDTO> findAll(int page, int pageSize);

    PageResponse<ClienteResponseDTO> findByNome(String nome, int page, int pageSize);

    ClienteResponseDTO findById(long id);

    ClienteResponseDTO findByCpf(String cpf);

    ClienteResponseDTO create(@NotNull(message = "Os dados devem ser informados.") @Valid ClienteRequestDTO dto);

    ClienteResponseDTO update(long id, @NotNull(message = "Os dados devem ser informados.") @Valid ClienteRequestDTO dto);

    void delete(long id);

    /** Valida regras de negócio e unicidade (CPF/e-mail) antes de criar a conta no Keycloak. */
    void validarNovoCadastro(@NotNull(message = "Os dados devem ser informados.") @Valid ClienteRequestDTO dto);

    /** Persiste o cliente já vinculado ao identificador (sub) criado no Keycloak. */
    ClienteResponseDTO criarVinculadoAoKeycloak(
            @NotNull(message = "Os dados devem ser informados.") @Valid ClienteRequestDTO dto,
            String keycloakId);

    /**
     * Localiza o cliente pelo keycloakId (ou vincula no primeiro acesso por e-mail).
     * Retorna null quando o usuário autenticado não possui cadastro de cliente (ex.: ADMIN puro).
     */
    ClienteResponseDTO vincularOuBuscarPorKeycloak(String keycloakId, String email);
}
