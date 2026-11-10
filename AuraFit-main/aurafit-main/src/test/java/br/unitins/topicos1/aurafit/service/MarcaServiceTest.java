package br.unitins.topicos1.aurafit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unitins.topicos1.aurafit.dto.MarcaRequestDTO;
import br.unitins.topicos1.aurafit.dto.MarcaResponseDTO;
import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.exception.ValidationException;
import br.unitins.topicos1.aurafit.model.Entidades;
import br.unitins.topicos1.aurafit.model.Marca;
import br.unitins.topicos1.aurafit.repository.MarcaRepository;
import br.unitins.topicos1.aurafit.repository.ProdutoRepository;

class MarcaServiceTest {

    private final MarcaServiceImpl service = new MarcaServiceImpl();
    private final MarcasEmMemoria marcas = new MarcasEmMemoria();
    private final ProdutosEmMemoria produtos = new ProdutosEmMemoria();

    @BeforeEach
    void preparar() {
        marcas.porId = null;
        marcas.porCnpj = null;
        marcas.persistida = null;
        marcas.excluida = null;
        produtos.possuiProdutos = false;
        service.marcaRepository = marcas;
        service.produtoRepository = produtos;
    }

    @Test
    void deveCadastrarMarcaNormalizandoDados() {
        MarcaResponseDTO resposta = service.create(
                new MarcaRequestDTO("  Growth  ", "11222333000181", " CONTATO@Growth.com ", true));

        assertSame(marcas.persistida.getNome(), resposta.nome());
        assertEquals("Growth", resposta.nome());
        assertEquals("contato@growth.com", resposta.emailContato());
        assertTrue(resposta.ativa());
    }

    @Test
    void deveRejeitarCnpjDuplicado() {
        marcas.porCnpj = new Marca();
        ValidationException erro = assertThrows(ValidationException.class,
                () -> service.create(new MarcaRequestDTO("Dux", "11222333000181", null, true)));
        assertEquals("cnpj", erro.getFieldErrors().get(0).field);
        assertNull(marcas.persistida);
    }

    @Test
    void deveAtualizarMarcaExistente() {
        marcas.porId = Entidades.comId(new Marca(), 7L);
        MarcaResponseDTO resposta = service.update(7L, new MarcaRequestDTO("Max", "45678912000155", "", false));
        assertEquals(7L, resposta.id());
        assertEquals("Max", resposta.nome());
        assertNull(resposta.emailContato());
        assertFalse(resposta.ativa());
    }

    @Test
    void naoDeveExcluirMarcaComProdutos() {
        marcas.porId = Entidades.comId(new Marca(), 1L);
        produtos.possuiProdutos = true;
        assertThrows(ValidationException.class, () -> service.delete(1L));
        assertNull(marcas.excluida);
    }

    @Test
    void deveExcluirMarcaSemProdutos() {
        marcas.porId = Entidades.comId(new Marca(), 1L);
        service.delete(1L);
        assertSame(marcas.porId, marcas.excluida);
    }

    @Test
    void deveRetornarNaoEncontrado() {
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }

    @Test
    void deveRejeitarPaginacaoInvalida() {
        assertThrows(ValidationException.class, () -> service.findAll(-1, 10));
        assertThrows(ValidationException.class, () -> service.findAll(0, 0));
        assertThrows(ValidationException.class, () -> service.findAll(0, 101));
    }

    static class MarcasEmMemoria extends MarcaRepository {
        Marca porId;
        Marca porCnpj;
        Marca persistida;
        Marca excluida;

        @Override
        public Marca findById(Long id) {
            return porId != null && id.equals(porId.getId()) ? porId : null;
        }

        @Override
        public Marca findByCnpjExceptId(String cnpj, Long id) {
            return porCnpj;
        }

        @Override
        public void persist(Marca marca) {
            persistida = marca;
        }

        @Override
        public void delete(Marca marca) {
            excluida = marca;
        }
    }

    static class ProdutosEmMemoria extends ProdutoRepository {
        boolean possuiProdutos;

        @Override
        public boolean existeComMarca(Long idMarca) {
            return possuiProdutos;
        }

        @Override
        public boolean existeComCategoria(Long idCategoria) {
            return possuiProdutos;
        }
    }
}
