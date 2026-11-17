package br.unitins.topicos1.fitwork.service;

import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.dto.ProdutoRequestDTO;
import br.unitins.topicos1.fitwork.dto.ProdutoResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface ProdutoService {

    PageResponse<ProdutoResponseDTO> findAll(int page, int pageSize);

    PageResponse<ProdutoResponseDTO> findByNome(String nome, int page, int pageSize);

    /**
     * Listagem com filtros opcionais: nome, tipo de proteína, categoria e marca.
     */
    PageResponse<ProdutoResponseDTO> buscar(String nome, Long idTipoProteina, Long idCategoria, Long idMarca,
            int page, int pageSize);

    /**
     * Listagem com filtros opcionais: nome, tipo de proteína, categoria, marca e status ativo.
     */
    PageResponse<ProdutoResponseDTO> buscar(String nome, Long idTipoProteina, Long idCategoria, Long idMarca,
            Boolean ativo, int page, int pageSize);

    ProdutoResponseDTO findById(long id);

    ProdutoResponseDTO create(@NotNull(message = "Os dados devem ser informados.") @Valid ProdutoRequestDTO dto);

    ProdutoResponseDTO update(long id, @NotNull(message = "Os dados devem ser informados.") @Valid ProdutoRequestDTO dto);

    void delete(long id);
}
