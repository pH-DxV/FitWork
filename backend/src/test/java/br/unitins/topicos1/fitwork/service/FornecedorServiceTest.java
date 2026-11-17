package br.unitins.topicos1.fitwork.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unitins.topicos1.fitwork.dto.FornecedorRequestDTO;
import br.unitins.topicos1.fitwork.exception.ResourceNotFoundException;
import br.unitins.topicos1.fitwork.exception.ValidationException;
import br.unitins.topicos1.fitwork.model.Entidades;
import br.unitins.topicos1.fitwork.model.Fornecedor;
import br.unitins.topicos1.fitwork.repository.FornecedorRepository;

class FornecedorServiceTest {

    private final FornecedorServiceImpl service = new FornecedorServiceImpl();
    private final FornecedoresEmMemoria fornecedores = new FornecedoresEmMemoria();
    private final MarcaServiceTest.ProdutosEmMemoria produtos = new MarcaServiceTest.ProdutosEmMemoria();

    @BeforeEach
    void preparar() {
        fornecedores.porId = null;
        fornecedores.porCnpj = null;
        fornecedores.persistido = null;
        fornecedores.excluido = null;
        produtos.possuiProdutos = false;
        service.fornecedorRepository = fornecedores;
        service.produtoRepository = produtos;
    }

    @Test
    void deveCadastrarFornecedorNormalizandoDados() {
        var resposta = service.create(
                new FornecedorRequestDTO("  Fit Norte Distribuidora  ", "11222333000181", "63999990001", " VENDAS@FitNorte.com "));
        assertEquals("Fit Norte Distribuidora", resposta.razaoSocial());
        assertEquals("11222333000181", resposta.cnpj());
        assertEquals("63999990001", resposta.telefone());
        assertEquals("vendas@fitnorte.com", resposta.email());
        assertSame(fornecedores.persistido.getRazaoSocial(), resposta.razaoSocial());
    }

    @Test
    void deveRejeitarCnpjDuplicado() {
        fornecedores.porCnpj = new Fornecedor();
        var erro = assertThrows(ValidationException.class,
                () -> service.create(new FornecedorRequestDTO("Outro Fornecedor", "11222333000181", "63999990001", null)));
        assertEquals("cnpj", erro.getFieldErrors().get(0).field);
        assertNull(fornecedores.persistido);
    }

    @Test
    void deveRejeitarRazaoSocialComMenosDeDoisCaracteresUteis() {
        var erro = assertThrows(ValidationException.class,
                () -> service.create(new FornecedorRequestDTO(" F ", "11222333000181", "63999990001", null)));
        assertEquals("razaoSocial", erro.getFieldErrors().get(0).field);
        assertNull(fornecedores.persistido);
    }

    @Test
    void naoDeveExcluirFornecedorComProdutos() {
        fornecedores.porId = Entidades.comId(new Fornecedor(), 2L);
        produtos.possuiProdutos = true;
        assertThrows(ValidationException.class, () -> service.delete(2L));
        assertNull(fornecedores.excluido);
    }

    @Test
    void deveExcluirFornecedorSemProdutos() {
        fornecedores.porId = Entidades.comId(new Fornecedor(), 2L);
        service.delete(2L);
        assertSame(fornecedores.porId, fornecedores.excluido);
    }

    @Test
    void deveRetornarNaoEncontrado() {
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    static class FornecedoresEmMemoria extends FornecedorRepository {
        Fornecedor porId;
        Fornecedor porCnpj;
        Fornecedor persistido;
        Fornecedor excluido;

        @Override
        public Fornecedor findById(Long id) {
            return porId != null && id.equals(porId.getId()) ? porId : null;
        }

        @Override
        public Fornecedor findByCnpjExceptId(String cnpj, Long id) {
            return porCnpj;
        }

        @Override
        public void persist(Fornecedor fornecedor) {
            persistido = fornecedor;
        }

        @Override
        public void delete(Fornecedor fornecedor) {
            excluido = fornecedor;
        }
    }
}
