package br.unitins.topicos1.aurafit.service;

import java.util.function.Supplier;
import br.unitins.topicos1.aurafit.dto.EstadoResponseDTO;
import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.mapper.EstadoMapper;
import br.unitins.topicos1.aurafit.model.Estado;
import br.unitins.topicos1.aurafit.repository.EstadoRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EstadoServiceImpl implements EstadoService {

    @Inject
    EstadoRepository estadoRepository;

    @Override
    public PageResponse<EstadoResponseDTO> findAll(int page, int pageSize) {
        return paginar(() -> estadoRepository.findAll(), page, pageSize);
    }

    @Override
    public PageResponse<EstadoResponseDTO> findByNome(String nome, int page, int pageSize) {
        return paginar(() -> estadoRepository.findByNome(nome), page, pageSize);
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

    private PageResponse<EstadoResponseDTO> paginar(Supplier<PanacheQuery<Estado>> consulta, int page, int pageSize) {
        Paginacao.validar(page, pageSize);
        PanacheQuery<Estado> query = consulta.get();
        return PageResponse.of(query.page(page, pageSize).list(), page, pageSize, query.count(),
                EstadoMapper::toResponse);
    }
}
