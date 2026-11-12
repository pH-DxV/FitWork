package br.unitins.topicos1.aurafit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unitins.topicos1.aurafit.dto.CategoriaRequestDTO;
import br.unitins.topicos1.aurafit.exception.ValidationException;
import br.unitins.topicos1.aurafit.model.Categoria;
import br.unitins.topicos1.aurafit.model.Entidades;
import br.unitins.topicos1.aurafit.repository.CategoriaRepository;

class CategoriaServiceTest {

    private final CategoriaServiceImpl service = new CategoriaServiceImpl();
    private final CategoriasEmMemoria categorias = new CategoriasEmMemoria();
    private final MarcaServiceTest.ProdutosEmMemoria produtos = new MarcaServiceTest.ProdutosEmMemoria();

    @BeforeEach
    void preparar() {
        categorias.porId = null;
        categorias.porNome = null;
        categorias.persistida = null;
        categorias.excluida = null;
        produtos.possuiProdutos = false;
        service.categoriaRepository = categorias;
        service.produtoRepository = produtos;
    }

    @Test
    void deveCadastrarCategoriaComDescricaoVaziaComoNula() {
        var resposta = service.create(new CategoriaRequestDTO(" Whey ", "   "));
        assertEquals("Whey", resposta.nome());
        assertNull(resposta.descricao());
        assertSame(categorias.persistida.getNome(), resposta.nome());
    }

    @Test
    void deveRejeitarNomeDuplicado() {
        categorias.porNome = new Categoria();
        var erro = assertThrows(ValidationException.class,
                () -> service.create(new CategoriaRequestDTO("Whey", null)));
        assertEquals("nome", erro.getFieldErrors().get(0).field);
    }

    @Test
    void deveRejeitarNomeComMenosDeDoisCaracteresUteis() {
        var erro = assertThrows(ValidationException.class,
                () -> service.create(new CategoriaRequestDTO(" a ", null)));
        assertEquals("nome", erro.getFieldErrors().get(0).field);
        assertNull(categorias.persistida);
    }

    @Test
    void naoDeveExcluirCategoriaComProdutos() {
        categorias.porId = Entidades.comId(new Categoria(), 3L);
        produtos.possuiProdutos = true;
        assertThrows(ValidationException.class, () -> service.delete(3L));
        assertNull(categorias.excluida);
    }

    static class CategoriasEmMemoria extends CategoriaRepository {
        Categoria porId;
        Categoria porNome;
        Categoria persistida;
        Categoria excluida;

        @Override
        public Categoria findById(Long id) {
            return porId != null && id.equals(porId.getId()) ? porId : null;
        }

        @Override
        public Categoria findByNomeExceptId(String nome, Long id) {
            return porNome;
        }

        @Override
        public void persist(Categoria categoria) {
            persistida = categoria;
        }

        @Override
        public void delete(Categoria categoria) {
            excluida = categoria;
        }
    }
}
