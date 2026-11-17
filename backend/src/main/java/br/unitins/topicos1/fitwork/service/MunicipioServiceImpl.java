package br.unitins.topicos1.fitwork.service;

import br.unitins.topicos1.fitwork.dto.MunicipioResponseDTO;
import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.exception.ResourceNotFoundException;
import br.unitins.topicos1.fitwork.exception.ValidationException;
import br.unitins.topicos1.fitwork.mapper.MunicipioMapper;
import br.unitins.topicos1.fitwork.model.Estado;
import br.unitins.topicos1.fitwork.model.Municipio;
import br.unitins.topicos1.fitwork.repository.EstadoRepository;
import br.unitins.topicos1.fitwork.repository.MunicipioRepository;
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
        return Paginacao.executar(() -> municipioRepository.findAll(), page, pageSize, MunicipioMapper::toResponse);
    }

    @Override
    public PageResponse<MunicipioResponseDTO> findByNome(String nome, int page, int pageSize) {
        return Paginacao.executar(() -> municipioRepository.findByNome(nome), page, pageSize,
                MunicipioMapper::toResponse);
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
}
