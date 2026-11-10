package br.unitins.topicos1.aurafit.service;

import java.util.function.Supplier;
import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.dto.SaborRequestDTO;
import br.unitins.topicos1.aurafit.dto.SaborResponseDTO;
import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.exception.ValidationException;
import br.unitins.topicos1.aurafit.mapper.SaborMapper;
import br.unitins.topicos1.aurafit.model.Sabor;
import br.unitins.topicos1.aurafit.repository.ProdutoRepository;
import br.unitins.topicos1.aurafit.repository.SaborRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SaborServiceImpl implements SaborService {

    @Inject
    SaborRepository saborRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Override
    public PageResponse<SaborResponseDTO> findAll(int page, int pageSize) {
        return paginar(() -> saborRepository.findAll(), page, pageSize);
    }

    @Override
    public PageResponse<SaborResponseDTO> findByNome(String nome, int page, int pageSize) {
        return paginar(() -> saborRepository.findByNome(nome), page, pageSize);
    }

    @Override
    public SaborResponseDTO findById(long id) {
        return SaborMapper.toResponse(buscarSaborOuFalhar(id));
    }

    @Override
    @Transactional
    public SaborResponseDTO create(SaborRequestDTO dto) {
        validarDados(dto, null);
        Sabor sabor = new Sabor();
        aplicarDados(sabor, dto);
        saborRepository.persist(sabor);
        return SaborMapper.toResponse(sabor);
    }

    @Override
    @Transactional
    public SaborResponseDTO update(long id, SaborRequestDTO dto) {
        Sabor sabor = buscarSaborOuFalhar(id);
        validarDados(dto, id);
        aplicarDados(sabor, dto);
        return SaborMapper.toResponse(sabor);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Sabor sabor = buscarSaborOuFalhar(id);
        if (produtoRepository.existeComSabor(id)) {
            throw ValidationException.of("id", "Este sabor está vinculado a produtos e não pode ser excluído.");
        }
        saborRepository.delete(sabor);
    }

    private PageResponse<SaborResponseDTO> paginar(Supplier<PanacheQuery<Sabor>> consulta, int page, int pageSize) {
        Paginacao.validar(page, pageSize);
        PanacheQuery<Sabor> query = consulta.get();
        return PageResponse.of(query.page(page, pageSize).list(), page, pageSize, query.count(),
                SaborMapper::toResponse);
    }

    private void validarDados(SaborRequestDTO dto, Long id) {
        if (saborRepository.findByNomeExceptId(dto.nome(), id) != null) {
            throw ValidationException.of("nome", "Já existe um sabor cadastrado com esse nome.");
        }
    }

    private void aplicarDados(Sabor sabor, SaborRequestDTO dto) {
        sabor.setNome(dto.nome().trim());
        sabor.setDescricao(Textos.limparOuNulo(dto.descricao()));
    }

    private Sabor buscarSaborOuFalhar(long id) {
        Sabor sabor = saborRepository.findById(id);
        if (sabor == null) {
            throw new ResourceNotFoundException("Sabor não encontrado.");
        }
        return sabor;
    }
}
