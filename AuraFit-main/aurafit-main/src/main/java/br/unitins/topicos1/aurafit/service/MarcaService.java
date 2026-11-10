package br.unitins.topicos1.aurafit.service;

import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.dto.MarcaRequestDTO;
import br.unitins.topicos1.aurafit.dto.MarcaResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface MarcaService {

    PageResponse<MarcaResponseDTO> findAll(int page, int pageSize);

    PageResponse<MarcaResponseDTO> findByNome(String nome, int page, int pageSize);

    MarcaResponseDTO findById(long id);

    MarcaResponseDTO create(@NotNull(message = "Os dados devem ser informados.") @Valid MarcaRequestDTO dto);

    MarcaResponseDTO update(long id, @NotNull(message = "Os dados devem ser informados.") @Valid MarcaRequestDTO dto);

    void delete(long id);
}
