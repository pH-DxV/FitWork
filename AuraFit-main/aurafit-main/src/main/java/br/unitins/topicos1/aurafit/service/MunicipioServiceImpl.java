package br.unitins.topicos1.aurafit.service;

import java.util.function.Supplier;
import br.unitins.topicos1.aurafit.dto.MunicipioResponseDTO;
import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.exception.ValidationException;
import br.unitins.topicos1.aurafit.mapper.MunicipioMapper;
import br.unitins.topicos1.aurafit.model.Estado;
import br.unitins.topicos1.aurafit.model.Municipio;
import br.unitins.topicos1.aurafit.repository.EstadoRepository;
import br.unitins.topicos1.aurafit.repository.MunicipioRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MunicipioServiceImpl implements MunicipioService {

    @Inject
    MunicipioRepository municipioRepository;

    @Inject
    EstadoRepository estadoRepository;

    @Override
    public PageResponse<MunicipioResponseDTO> findAll(int page, int pageSize) {
        return paginar(() -> municipioRepository.findAll(), page, pageSize);
    }

    @Override
    public PageResponse<MunicipioResponseDTO> findByNome(String nome, int page, int pageSize) {
        return paginar(() -> municipioRepository.findByNome(nome), page, pageSize);
    }

    @Override
    public MunicipioResponseDTO findById(long id) {
        Municipio municipio = municipioRepository.findById(id);
        if (municipio == null) {
            throw new ResourceNotFoundException("Município não encontrado.");
        }
        return MunicipioMapper.toResponse(municipio);
    }

    @Override
    @Transactional
    public Municipio buscarOuCriar(String nome, String uf, String codigoIbge) {
        Estado estado = estadoRepository.findBySigla(uf);
        if (estado == null) {
            throw ValidationException.of("endereco.uf", "UF não encontrada.");
        }

        Municipio municipio = null;
        if (codigoIbge != null && !codigoIbge.isBlank()) {
            municipio = municipioRepository.findByCodigoIbge(codigoIbge);
        }
        if (municipio == null) {
            municipio = municipioRepository.findByNomeEEstado(nome, estado.getId());
        }

        if (municipio != null) {
            if (!municipio.getEstado().getId().equals(estado.getId())) {
                throw ValidationException.of("endereco.uf", "A UF não corresponde ao município informado.");
            }
            if (municipio.getCodigoIbge() == null && codigoIbge != null && !codigoIbge.isBlank()) {
                municipio.setCodigoIbge(codigoIbge);
            }
            return municipio;
        }

        municipio = new Municipio();
        municipio.setNome(nome.trim());
        municipio.setCodigoIbge(codigoIbge == null || codigoIbge.isBlank() ? null : codigoIbge);
        municipio.setEstado(estado);
        municipioRepository.persist(municipio);
        return municipio;
    }

    private PageResponse<MunicipioResponseDTO> paginar(Supplier<PanacheQuery<Municipio>> consulta, int page, int pageSize) {
        Paginacao.validar(page, pageSize);
        PanacheQuery<Municipio> query = consulta.get();
        return PageResponse.of(query.page(page, pageSize).list(), page, pageSize, query.count(),
                MunicipioMapper::toResponse);
    }
}
