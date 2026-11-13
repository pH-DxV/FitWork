package br.unitins.topicos1.aurafit.service;

import br.unitins.topicos1.aurafit.dto.EstadoResponseDTO;
import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.mapper.EstadoMapper;
import br.unitins.topicos1.aurafit.model.Estado;
import br.unitins.topicos1.aurafit.repository.EstadoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EstadoServiceImpl implements EstadoService {

    @Inject
    EstadoRepository estadoRepository;

    @Override
    public PageResponse<EstadoResponseDTO> findAll(int page, int pageSize) {
        return Paginacao.executar(() -> estadoRepository.findAll(), page, pageSize, EstadoMapper::toResponse);
    }

    @Override
    public PageResponse<EstadoResponseDTO> findByNome(String nome, int page, int pageSize) {
        return Paginacao.executar(() -> estadoRepository.findByNome(nome), page, pageSize, EstadoMapper::toResponse);
    }

    @Override
    public EstadoResponseDTO findById(long id) {
        Estado estado = estadoRepository.findById(id);
        if (estado == null) {
            throw new ResourceNotFoundException("Estado não encontrado.");
        }
        return EstadoMapper.toResponse(estado);
    }

    @Override
    public EstadoResponseDTO findBySigla(String sigla) {
        Estado estado = estadoRepository.findBySigla(sigla);
        if (estado == null) {
            throw new ResourceNotFoundException("Estado não encontrado.");
        }
        return EstadoMapper.toResponse(estado);
    }
}
