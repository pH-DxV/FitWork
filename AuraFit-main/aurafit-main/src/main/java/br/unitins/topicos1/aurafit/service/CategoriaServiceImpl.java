package br.unitins.topicos1.aurafit.service;

import br.unitins.topicos1.aurafit.dto.CategoriaRequestDTO;
import br.unitins.topicos1.aurafit.dto.CategoriaResponseDTO;
import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.exception.ValidationException;
import br.unitins.topicos1.aurafit.mapper.CategoriaMapper;
import br.unitins.topicos1.aurafit.model.Categoria;
import br.unitins.topicos1.aurafit.repository.CategoriaRepository;
import br.unitins.topicos1.aurafit.repository.ProdutoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CategoriaServiceImpl implements CategoriaService {

    @Inject
    CategoriaRepository categoriaRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Override
    public PageResponse<CategoriaResponseDTO> findAll(int page, int pageSize) {
        return Paginacao.executar(() -> categoriaRepository.findAll(), page, pageSize, CategoriaMapper::toResponse);
    }

    @Override
    public PageResponse<CategoriaResponseDTO> findByNome(String nome, int page, int pageSize) {
        return Paginacao.executar(() -> categoriaRepository.findByNome(nome), page, pageSize,
                CategoriaMapper::toResponse);
    }

    @Override
    public CategoriaResponseDTO findById(long id) {
        return CategoriaMapper.toResponse(buscarCategoriaOuFalhar(id));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO create(CategoriaRequestDTO dto) {
        validarDados(dto, null);
        Categoria categoria = new Categoria();
        aplicarDados(categoria, dto);
        categoriaRepository.persist(categoria);
        return CategoriaMapper.toResponse(categoria);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO update(long id, CategoriaRequestDTO dto) {
        Categoria categoria = buscarCategoriaOuFalhar(id);
        validarDados(dto, id);
        aplicarDados(categoria, dto);
        return CategoriaMapper.toResponse(categoria);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Categoria categoria = buscarCategoriaOuFalhar(id);
        if (produtoRepository.existeComCategoria(id)) {
            throw ValidationException.conflict("id",
                    "Esta categoria possui produtos cadastrados e não pode ser excluída.");
        }
        categoriaRepository.delete(categoria);
    }

    private void validarDados(CategoriaRequestDTO dto, Long id) {
        Textos.validarTamanhoUtil(dto.nome(), "nome", 2, 100, "O nome deve conter entre 2 e 100 caracteres.");
        if (categoriaRepository.findByNomeExceptId(dto.nome(), id) != null) {
            throw ValidationException.conflict("nome", "Já existe uma categoria cadastrada com esse nome.");
        }
    }

    private void aplicarDados(Categoria categoria, CategoriaRequestDTO dto) {
        categoria.setNome(dto.nome().trim());
        categoria.setDescricao(Textos.limparOuNulo(dto.descricao()));
    }

    private Categoria buscarCategoriaOuFalhar(long id) {
        Categoria categoria = categoriaRepository.findById(id);
        if (categoria == null) {
            throw new ResourceNotFoundException("Categoria não encontrada.");
        }
        return categoria;
    }
}
