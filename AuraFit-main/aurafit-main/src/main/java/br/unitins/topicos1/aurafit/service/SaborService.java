package br.unitins.topicos1.aurafit.service;

import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.dto.SaborRequestDTO;
import br.unitins.topicos1.aurafit.dto.SaborResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface SaborService {

    PageResponse<SaborResponseDTO> findAll(int page, int pageSize);

    PageResponse<SaborResponseDTO> findByNome(String nome, int page, int pageSize);

    SaborResponseDTO findById(long id);

    SaborResponseDTO create(@NotNull(message = "Os dados devem ser informados.") @Valid SaborRequestDTO dto);

    SaborResponseDTO update(long id, @NotNull(message = "Os dados devem ser informados.") @Valid SaborRequestDTO dto);

    void delete(long id);
}
