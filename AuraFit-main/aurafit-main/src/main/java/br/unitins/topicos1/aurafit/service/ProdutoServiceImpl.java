package br.unitins.topicos1.aurafit.service;

import java.util.HashSet;
import java.util.Set;

import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.dto.ProdutoRequestDTO;
import br.unitins.topicos1.aurafit.dto.ProdutoResponseDTO;
import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.exception.ValidationException;
import br.unitins.topicos1.aurafit.mapper.ProdutoMapper;
import br.unitins.topicos1.aurafit.model.Categoria;
import br.unitins.topicos1.aurafit.model.Fornecedor;
import br.unitins.topicos1.aurafit.model.Marca;
import br.unitins.topicos1.aurafit.model.Produto;
import br.unitins.topicos1.aurafit.model.Sabor;
import br.unitins.topicos1.aurafit.model.TipoProteina;
import br.unitins.topicos1.aurafit.repository.CategoriaRepository;
import br.unitins.topicos1.aurafit.repository.FornecedorRepository;
import br.unitins.topicos1.aurafit.repository.MarcaRepository;
import br.unitins.topicos1.aurafit.repository.ProdutoRepository;
import br.unitins.topicos1.aurafit.repository.SaborRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProdutoServiceImpl implements ProdutoService {

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    CategoriaRepository categoriaRepository;

    @Inject
    MarcaRepository marcaRepository;

    @Inject
    FornecedorRepository fornecedorRepository;

    @Inject
    SaborRepository saborRepository;

    @Override
    @Transactional
    public PageResponse<ProdutoResponseDTO> findAll(int page, int pageSize) {
        return Paginacao.executar(() -> produtoRepository.findAll(), page, pageSize, ProdutoMapper::toResponse);
    }

    @Override
    @Transactional
    public PageResponse<ProdutoResponseDTO> findByNome(String nome, int page, int pageSize) {
        return Paginacao.executar(() -> produtoRepository.findByNome(nome), page, pageSize,
                ProdutoMapper::toResponse);
    }

    @Override
    @Transactional
    public PageResponse<ProdutoResponseDTO> buscar(String nome, Long idTipoProteina, Long idCategoria, Long idMarca,
            int page, int pageSize) {
        return buscar(nome, idTipoProteina, idCategoria, idMarca, null, page, pageSize);
    }

    @Override
    @Transactional
    public PageResponse<ProdutoResponseDTO> buscar(String nome, Long idTipoProteina, Long idCategoria, Long idMarca,
            Boolean ativo, int page, int pageSize) {
        TipoProteina tipo = null;
        if (idTipoProteina != null) {
            tipo = TipoProteina.valueOf(idTipoProteina);
            if (tipo == null) {
                throw ValidationException.of("idTipoProteina", "Tipo de proteína inválido.");
            }
        }
        TipoProteina filtroTipo = tipo;
        return Paginacao.executar(() -> produtoRepository.buscar(nome, filtroTipo, idCategoria, idMarca, ativo),
                page, pageSize, ProdutoMapper::toResponse);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO findById(long id) {
        return ProdutoMapper.toResponse(buscarProdutoOuFalhar(id));
    }

    @Override
    @Transactional
    public ProdutoResponseDTO create(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        aplicarDados(produto, dto);
        produtoRepository.persist(produto);
        return ProdutoMapper.toResponse(produto);
    }

    @Override
    @Transactional
    public ProdutoResponseDTO update(long id, ProdutoRequestDTO dto) {
        Produto produto = buscarProdutoOuFalhar(id);
        aplicarDados(produto, dto);
        return ProdutoMapper.toResponse(produto);
    }

    @Override
    @Transactional
    public void delete(long id) {
        produtoRepository.delete(buscarProdutoOuFalhar(id));
    }

    private void aplicarDados(Produto produto, ProdutoRequestDTO dto) {
        Textos.validarTamanhoUtil(dto.nome(), "nome", 2, 120, "O nome deve conter entre 2 e 120 caracteres.");

        Marca marca = buscarMarca(dto.idMarca());
        boolean trocouDeMarca = produto.getMarca() == null || !produto.getMarca().getId().equals(marca.getId());
        if (trocouDeMarca && !marca.isAtiva()) {
            throw ValidationException.of("idMarca", "Esta marca está inativa e não pode receber novos produtos.");
        }

        produto.setNome(dto.nome().trim());
        produto.setDescricao(Textos.limparOuNulo(dto.descricao()));
        produto.setPreco(dto.preco());
        produto.setPesoGramas(dto.pesoGramas());
        produto.setEstoque(dto.estoque());
        produto.setTipoProteina(buscarTipoProteina(dto.idTipoProteina()));
        produto.setAtivo(dto.ativo());
        produto.setCategoria(buscarCategoria(dto.idCategoria()));
        produto.setMarca(marca);
        produto.setFornecedor(buscarFornecedor(dto.idFornecedor()));
        produto.setSabores(buscarSabores(dto.idsSabores()));
    }

    private TipoProteina buscarTipoProteina(Long id) {
        TipoProteina tipo = TipoProteina.valueOf(id);
        if (tipo == null) {
            throw ValidationException.of("idTipoProteina", "Tipo de proteína inválido.");
        }
        return tipo;
    }

    private Categoria buscarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id);
        if (categoria == null) {
            throw ValidationException.of("idCategoria", "Categoria não encontrada.");
        }
        return categoria;
    }

    private Marca buscarMarca(Long id) {
        Marca marca = marcaRepository.findById(id);
        if (marca == null) {
            throw ValidationException.of("idMarca", "Marca não encontrada.");
        }
        return marca;
    }

    private Fornecedor buscarFornecedor(Long id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id);
        if (fornecedor == null) {
            throw ValidationException.of("idFornecedor", "Fornecedor não encontrado.");
        }
        return fornecedor;
    }

    private Set<Sabor> buscarSabores(Set<Long> ids) {
        Set<Sabor> sabores = new HashSet<>();
        if (ids == null || ids.isEmpty()) {
            return sabores;
        }
        for (Long id : ids) {
            if (id == null) {
                throw ValidationException.of("idsSabores", "Sabor não encontrado: null.");
            }
        }
        for (Sabor sabor : saborRepository.findByIds(ids)) {
            if (sabor != null) {
                sabores.add(sabor);
            }
        }
        if (sabores.size() != ids.size()) {
            Set<Long> encontrados = new HashSet<>();
            for (Sabor s : sabores) {
                encontrados.add(s.getId());
            }
            for (Long id : ids) {
                if (!encontrados.contains(id)) {
                    throw ValidationException.of("idsSabores", "Sabor não encontrado: " + id + ".");
                }
            }
        }
        return sabores;
    }

    private Produto buscarProdutoOuFalhar(long id) {
        Produto produto = produtoRepository.findById(id);
        if (produto == null) {
            throw new ResourceNotFoundException("Produto não encontrado.");
        }
        return produto;
    }
}
