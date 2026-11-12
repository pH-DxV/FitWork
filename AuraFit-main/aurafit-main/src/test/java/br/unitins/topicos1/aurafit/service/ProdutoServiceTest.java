package br.unitins.topicos1.aurafit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unitins.topicos1.aurafit.dto.ProdutoRequestDTO;
import br.unitins.topicos1.aurafit.exception.ValidationException;
import br.unitins.topicos1.aurafit.model.Categoria;
import br.unitins.topicos1.aurafit.model.Entidades;
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

class ProdutoServiceTest {

    private final ProdutoServiceImpl service = new ProdutoServiceImpl();
    private final Map<Long, Marca> marcas = new HashMap<>();
    private final Map<Long, Sabor> sabores = new HashMap<>();
    private Produto persistido;
    private Produto existente;

    private final Categoria categoria = Entidades.comId(new Categoria(), 1L);
    private final Fornecedor fornecedor = Entidades.comId(new Fornecedor(), 1L);

    @BeforeEach
    void preparar() {
        persistido = null;
        existente = null;
        marcas.clear();
        sabores.clear();

        categoria.setNome("Whey Protein");
        fornecedor.setRazaoSocial("Fit Norte");

        Marca ativa = Entidades.comId(new Marca(), 1L);
        ativa.setNome("Growth");
        Marca inativa = Entidades.comId(new Marca(), 2L);
        inativa.setNome("Antiga");
        inativa.setAtiva(false);
        marcas.put(1L, ativa);
        marcas.put(2L, inativa);

        Sabor chocolate = Entidades.comId(new Sabor(), 1L);
        chocolate.setNome("Chocolate");
        Sabor baunilha = Entidades.comId(new Sabor(), 2L);
        baunilha.setNome("Baunilha");
        sabores.put(1L, chocolate);
        sabores.put(2L, baunilha);

        service.produtoRepository = new ProdutoRepository() {
            @Override
            public void persist(Produto produto) {
                persistido = produto;
            }

            @Override
            public Produto findById(Long id) {
                return existente;
            }
        };
        service.categoriaRepository = new CategoriaRepository() {
            @Override
            public Categoria findById(Long id) {
                return id == 1L ? categoria : null;
            }
        };
        service.fornecedorRepository = new FornecedorRepository() {
            @Override
            public Fornecedor findById(Long id) {
                return id == 1L ? fornecedor : null;
            }
        };
        service.marcaRepository = new MarcaRepository() {
            @Override
            public Marca findById(Long id) {
                return marcas.get(id);
            }
        };
        service.saborRepository = new SaborRepository() {
            @Override
            public Sabor findById(Long id) {
                return sabores.get(id);
            }

            @Override
            public java.util.List<Sabor> findByIds(java.util.Collection<Long> ids) {
                return ids.stream().map(sabores::get).filter(java.util.Objects::nonNull).toList();
            }
        };
    }

    @Test
    void deveCadastrarProdutoResolvendoRelacionamentos() {
        var resposta = service.create(dto(1L, 2L, Set.of(1L, 2L)));

        assertSame(categoria, persistido.getCategoria());
        assertSame(fornecedor, persistido.getFornecedor());
        assertEquals(TipoProteina.ISOLADO, resposta.tipoProteina());
        assertEquals("Growth", resposta.marca().nome());
        assertEquals(2, resposta.sabores().size());
        // sabores voltam ordenados por nome
        assertEquals("Baunilha", resposta.sabores().get(0).nome());
    }

    @Test
    void deveRejeitarMarcaInativaEmProdutoNovo() {
        var erro = assertThrows(ValidationException.class, () -> service.create(dto(2L, 1L, Set.of())));
        assertEquals("idMarca", erro.getFieldErrors().get(0).field);
        assertNull(persistido);
    }

    @Test
    void devePermitirManterMarcaInativaAoEditar() {
        existente = Entidades.comId(new Produto(), 5L);
        existente.setMarca(marcas.get(2L));
        var resposta = service.update(5L, dto(2L, 1L, null));
        assertEquals("Antiga", resposta.marca().nome());
    }

    @Test
    void deveRejeitarTipoProteinaESaborInexistentes() {
        assertEquals("idTipoProteina", assertThrows(ValidationException.class,
                () -> service.create(dto(1L, 99L, Set.of()))).getFieldErrors().get(0).field);
        assertEquals("idsSabores", assertThrows(ValidationException.class,
                () -> service.create(dto(1L, 1L, Set.of(42L)))).getFieldErrors().get(0).field);
    }

    @Test
    void deveRejeitarCategoriaMarcaEFornecedorInexistentes() {
        var semCategoria = new ProdutoRequestDTO("Whey 900g", null, new BigDecimal("199.90"), 900, 10,
                1L, true, 99L, 1L, 1L, Set.of());
        assertEquals("idCategoria", assertThrows(ValidationException.class,
                () -> service.create(semCategoria)).getFieldErrors().get(0).field);

        var semMarca = new ProdutoRequestDTO("Whey 900g", null, new BigDecimal("199.90"), 900, 10,
                1L, true, 1L, 99L, 1L, Set.of());
        assertEquals("idMarca", assertThrows(ValidationException.class,
                () -> service.create(semMarca)).getFieldErrors().get(0).field);

        var semFornecedor = new ProdutoRequestDTO("Whey 900g", null, new BigDecimal("199.90"), 900, 10,
                1L, true, 1L, 1L, 99L, Set.of());
        assertEquals("idFornecedor", assertThrows(ValidationException.class,
                () -> service.create(semFornecedor)).getFieldErrors().get(0).field);
    }

    @Test
    void deveRejeitarNomeComApenasUmCaractereAposTrim() {
        var nomeCurto = new ProdutoRequestDTO(" a ", null, new BigDecimal("199.90"), 900, 10,
                1L, true, 1L, 1L, 1L, Set.of());
        assertEquals("nome", assertThrows(ValidationException.class,
                () -> service.create(nomeCurto)).getFieldErrors().get(0).field);
    }

    @Test
    void deveRejeitarFiltroComTipoInvalido() {
        assertThrows(ValidationException.class, () -> service.buscar(null, 77L, null, null, 0, 10));
    }

    private ProdutoRequestDTO dto(Long idMarca, Long idTipo, Set<Long> sabores) {
        return new ProdutoRequestDTO("  Whey Isolado 900g ", "Descrição", new BigDecimal("199.90"), 900, 10,
                idTipo, true, 1L, idMarca, 1L, sabores);
    }
}
