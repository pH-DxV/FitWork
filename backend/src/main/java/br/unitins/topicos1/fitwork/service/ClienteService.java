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
}
