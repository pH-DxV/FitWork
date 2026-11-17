package br.unitins.topicos1.fitwork.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unitins.topicos1.fitwork.dto.SaborRequestDTO;
import br.unitins.topicos1.fitwork.exception.ResourceNotFoundException;
import br.unitins.topicos1.fitwork.exception.ValidationException;
import br.unitins.topicos1.fitwork.model.Entidades;
import br.unitins.topicos1.fitwork.model.Sabor;
import br.unitins.topicos1.fitwork.repository.SaborRepository;

class SaborServiceTest {

    private final SaborServiceImpl service = new SaborServiceImpl();
    private final SaboresEmMemoria sabores = new SaboresEmMemoria();
    private final MarcaServiceTest.ProdutosEmMemoria produtos = new MarcaServiceTest.ProdutosEmMemoria();

    @BeforeEach
    void preparar() {
        sabores.porId = null;
        sabores.porNome = null;
        sabores.persistido = null;
        sabores.excluido = null;
        produtos.possuiProdutos = false;
        service.saborRepository = sabores;
        service.produtoRepository = produtos;
    }

    @Test
    void deveCadastrarSaborNormalizandoDados() {
        var resposta = service.create(new SaborRequestDTO("  Chocolate Belga  ", "   "));
        assertEquals("Chocolate Belga", resposta.nome());
        assertNull(resposta.descricao());
        assertSame(sabores.persistido.getNome(), resposta.nome());
    }

    @Test
    void deveRejeitarNomeDuplicado() {
        sabores.porNome = new Sabor();
        var erro = assertThrows(ValidationException.class,
                () -> service.create(new SaborRequestDTO("Chocolate", null)));
        assertEquals("nome", erro.getFieldErrors().get(0).field);
        assertNull(sabores.persistido);
    }

    @Test
    void deveRejeitarNomeComMenosDeDoisCaracteresUteis() {
        var erro = assertThrows(ValidationException.class,
                () -> service.create(new SaborRequestDTO(" x ", null)));
        assertEquals("nome", erro.getFieldErrors().get(0).field);
        assertNull(sabores.persistido);
    }

    @Test
    void naoDeveExcluirSaborComProdutos() {
        sabores.porId = Entidades.comId(new Sabor(), 4L);
        produtos.possuiProdutos = true;
        assertThrows(ValidationException.class, () -> service.delete(4L));
        assertNull(sabores.excluido);
    }

    @Test
    void deveExcluirSaborSemProdutos() {
        sabores.porId = Entidades.comId(new Sabor(), 4L);
        service.delete(4L);
        assertSame(sabores.porId, sabores.excluido);
    }

    @Test
    void deveRetornarNaoEncontrado() {
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    static class SaboresEmMemoria extends SaborRepository {
        Sabor porId;
        Sabor porNome;
        Sabor persistido;
        Sabor excluido;

        @Override
        public Sabor findById(Long id) {
            return porId != null && id.equals(porId.getId()) ? porId : null;
        }

        @Override
        public Sabor findByNomeExceptId(String nome, Long id) {
            return porNome;
        }

        @Override
        public void persist(Sabor sabor) {
            persistido = sabor;
        }

        @Override
        public void delete(Sabor sabor) {
            excluido = sabor;
        }
    }
}
