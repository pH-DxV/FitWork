package br.unitins.topicos1.fitwork.service;

import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.dto.CategoriaRequestDTO;
import br.unitins.topicos1.fitwork.dto.CategoriaResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface CategoriaService {

    PageResponse<CategoriaResponseDTO> findAll(int page, int pageSize);

    PageResponse<CategoriaResponseDTO> findByNome(String nome, int page, int pageSize);

    CategoriaResponseDTO findById(long id);

    CategoriaResponseDTO create(@NotNull(message = "Os dados devem ser informados.") @Valid CategoriaRequestDTO dto);

    CategoriaResponseDTO update(long id, @NotNull(message = "Os dados devem ser informados.") @Valid CategoriaRequestDTO dto);

    void delete(long id);
}
