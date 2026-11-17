package br.unitins.topicos1.fitwork.service;

import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.dto.FornecedorRequestDTO;
import br.unitins.topicos1.fitwork.dto.FornecedorResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface FornecedorService {

    PageResponse<FornecedorResponseDTO> findAll(int page, int pageSize);

    PageResponse<FornecedorResponseDTO> findByNome(String nome, int page, int pageSize);

    FornecedorResponseDTO findById(long id);

    FornecedorResponseDTO create(@NotNull(message = "Os dados devem ser informados.") @Valid FornecedorRequestDTO dto);

    FornecedorResponseDTO update(long id, @NotNull(message = "Os dados devem ser informados.") @Valid FornecedorRequestDTO dto);

    void delete(long id);
}
