package br.unitins.topicos1.aurafit.service;

import br.unitins.topicos1.aurafit.dto.EstadoResponseDTO;
import br.unitins.topicos1.aurafit.dto.PageResponse;

public interface EstadoService {

    PageResponse<EstadoResponseDTO> findAll(int page, int pageSize);

    PageResponse<EstadoResponseDTO> findByNome(String nome, int page, int pageSize);

    EstadoResponseDTO findById(long id);

    EstadoResponseDTO findBySigla(String sigla);
}
