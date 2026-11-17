package br.unitins.topicos1.fitwork.service;

import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.dto.SaborRequestDTO;
import br.unitins.topicos1.fitwork.dto.SaborResponseDTO;
import br.unitins.topicos1.fitwork.exception.ResourceNotFoundException;
import br.unitins.topicos1.fitwork.exception.ValidationException;
import br.unitins.topicos1.fitwork.mapper.SaborMapper;
import br.unitins.topicos1.fitwork.model.Sabor;
import br.unitins.topicos1.fitwork.repository.ProdutoRepository;
import br.unitins.topicos1.fitwork.repository.SaborRepository;
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
        return Paginacao.executar(() -> saborRepository.findAll(), page, pageSize, SaborMapper::toResponse);
    }

    @Override
    public PageResponse<SaborResponseDTO> findByNome(String nome, int page, int pageSize) {
        return Paginacao.executar(() -> saborRepository.findByNome(nome), page, pageSize, SaborMapper::toResponse);
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
            throw ValidationException.conflict("id",
                    "Este sabor está vinculado a produtos e não pode ser excluído.");
        }
        saborRepository.delete(sabor);
    }

    private void validarDados(SaborRequestDTO dto, Long id) {
        Textos.validarTamanhoUtil(dto.nome(), "nome", 2, 80, "O nome deve conter entre 2 e 80 caracteres.");
        if (saborRepository.findByNomeExceptId(dto.nome(), id) != null) {
            throw ValidationException.conflict("nome", "Já existe um sabor cadastrado com esse nome.");
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
