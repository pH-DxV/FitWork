package br.unitins.topicos1.fitwork.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unitins.topicos1.fitwork.dto.ClienteRequestDTO;
import br.unitins.topicos1.fitwork.dto.EnderecoRequestDTO;
import br.unitins.topicos1.fitwork.dto.TelefoneRequestDTO;
import br.unitins.topicos1.fitwork.exception.ValidationException;
import br.unitins.topicos1.fitwork.model.Cliente;
import br.unitins.topicos1.fitwork.model.Entidades;
import br.unitins.topicos1.fitwork.model.Estado;
import br.unitins.topicos1.fitwork.model.Municipio;
import br.unitins.topicos1.fitwork.model.Pessoa;
import br.unitins.topicos1.fitwork.model.Regiao;
import br.unitins.topicos1.fitwork.repository.ClienteRepository;
import br.unitins.topicos1.fitwork.repository.EstadoRepository;
import br.unitins.topicos1.fitwork.repository.MunicipioRepository;
import br.unitins.topicos1.fitwork.repository.PessoaRepository;

class ClienteServiceTest {

    private final ClienteServiceImpl service = new ClienteServiceImpl();
    private final MunicipioServiceImpl municipioService = new MunicipioServiceImpl();

    private final Estado tocantins = Entidades.comId(new Estado(), 27L);
    private final Municipio palmas = Entidades.comId(new Municipio(), 1L);

    private Cliente persistido;
    private Cliente existente;
    private Cliente excluido;
    private Pessoa pessoaExcluida;
    private Pessoa pessoaComMesmoCpf;
    private Pessoa pessoaComMesmoEmail;
    private Municipio municipioCriado;

    @BeforeEach
    void preparar() {
        persistido = null;
        existente = null;
        excluido = null;
        pessoaExcluida = null;
        pessoaComMesmoCpf = null;
        pessoaComMesmoEmail = null;
        municipioCriado = null;

        tocantins.setNome("Tocantins");
        tocantins.setSigla("TO");
        tocantins.setRegiao(Regiao.NORTE);
        palmas.setNome("Palmas");
        palmas.setCodigoIbge("1721000");
        palmas.setEstado(tocantins);

        Estado goias = Entidades.comId(new Estado(), 9L);
        goias.setNome("Goiás");
        goias.setSigla("GO");
        goias.setRegiao(Regiao.CENTRO_OESTE);

        municipioService.estadoRepository = new EstadoRepository() {
            @Override
            public Estado findBySigla(String sigla) {
                if ("TO".equalsIgnoreCase(sigla)) return tocantins;
                if ("GO".equalsIgnoreCase(sigla)) return goias;
                return null;
            }
        };
        municipioService.municipioRepository = new MunicipioRepository() {
            @Override
            public Municipio findByCodigoIbge(String codigoIbge) {
                return "1721000".equals(codigoIbge) ? palmas : null;
            }

            @Override
            public Municipio findByNomeEEstado(String nome, Long idEstado) {
                return null;
            }

            @Override
            public void persist(Municipio municipio) {
                municipioCriado = municipio;
            }
        };

        service.municipioService = municipioService;
        service.clienteRepository = new ClienteRepository() {
            @Override
            public void persist(Cliente cliente) {
                persistido = cliente;
            }

            @Override
            public Cliente findById(Long id) {
                return existente;
            }

            @Override
            public Cliente findByCpf(String cpf) {
                return existente != null && existente.getPessoa() != null
                        && cpf.equals(existente.getPessoa().getCpf()) ? existente : null;
            }

            @Override
            public void delete(Cliente cliente) {
                excluido = cliente;
            }
        };
        service.pessoaRepository = new PessoaRepository() {
            @Override
            public Pessoa findByCpfExceptId(String cpf, Long idPessoa) {
                return pessoaComMesmoCpf;
            }

            @Override
            public Pessoa findByEmailExceptId(String email, Long idPessoa) {
                return pessoaComMesmoEmail;
            }

            @Override
            public void delete(Pessoa pessoa) {
                pessoaExcluida = pessoa;
            }
        };
    }

    @Test
    void deveCadastrarClienteComPessoaEnderecoETelefones() {
        var resposta = service.create(dto(LocalDate.of(1995, 5, 10), "1721000", "Palmas"));

        Pessoa pessoa = persistido.getPessoa();
        assertEquals("Ana Martins", pessoa.getNome());
        assertEquals("ana@fitwork.com", pessoa.getEmail());
        assertSame(palmas, pessoa.getEndereco().getMunicipio());
        assertSame(pessoa, pessoa.getTelefones().get(0).getPessoa());
        assertNull(pessoa.getEndereco().getComplemento());
        assertEquals("TO", resposta.endereco().municipio().estado().sigla());
        assertTrue(resposta.aceitaNovidades());
    }

    @Test
    void deveCriarMunicipioQuandoNaoExiste() {
        service.create(dto(LocalDate.of(1995, 5, 10), "1702109", "Araguaína"));
        assertNotNull(municipioCriado);
        assertEquals("Araguaína", municipioCriado.getNome());
        assertEquals("1702109", municipioCriado.getCodigoIbge());
        assertSame(tocantins, municipioCriado.getEstado());
    }

    @Test
    void deveRejeitarClienteMenorQueIdadeMinima() {
        var erro = assertThrows(ValidationException.class,
                () -> service.create(dto(LocalDate.now().minusYears(10), "1721000", "Palmas")));
        assertEquals("dataNascimento", erro.getFieldErrors().get(0).field);
        assertNull(persistido);
    }

    @Test
    void deveRejeitarCpfDuplicado() {
        pessoaComMesmoCpf = new Pessoa();
        var erro = assertThrows(ValidationException.class,
                () -> service.create(dto(LocalDate.of(1995, 5, 10), "1721000", "Palmas")));
        assertEquals("cpf", erro.getFieldErrors().get(0).field);
    }

    @Test
    void deveRejeitarEmailDuplicado() {
        pessoaComMesmoEmail = new Pessoa();
        var erro = assertThrows(ValidationException.class,
                () -> service.create(dto(LocalDate.of(1995, 5, 10), "1721000", "Palmas")));
        assertEquals("email", erro.getFieldErrors().get(0).field);
    }

    @Test
    void deveRejeitarUfInexistente() {
        var dto = new ClienteRequestDTO("Ana", "ana@fitwork.com", "52998224725", LocalDate.of(1995, 5, 10), null,
                List.of(new TelefoneRequestDTO("63", "999991111", true)),
                new EnderecoRequestDTO("77001002", "Rua A", "1", null, "Centro", "Cidade", "XX", null),
                false, true);
        var erro = assertThrows(ValidationException.class, () -> service.create(dto));
        assertEquals("endereco.uf", erro.getFieldErrors().get(0).field);
    }

    @Test
    void deveRejeitarUfDivergenteDoMunicipioEncontradoPorIbge() {
        var dto = new ClienteRequestDTO("Ana", "ana@fitwork.com", "52998224725", LocalDate.of(1995, 5, 10), null,
                List.of(new TelefoneRequestDTO("63", "999991111", true)),
                new EnderecoRequestDTO("77001002", "Rua A", "1", null, "Centro", "Palmas", "GO", "1721000"),
                false, true);
        var erro = assertThrows(ValidationException.class, () -> service.create(dto));
        assertEquals("endereco.uf", erro.getFieldErrors().get(0).field);
    }

    @Test
    void deveBuscarPorCpfFormatadoEExcluirClienteComPessoa() {
        service.create(dto(LocalDate.of(1995, 5, 10), "1721000", "Palmas"));
        existente = Entidades.comId(persistido, 10L);
        Entidades.comId(existente.getPessoa(), 20L);

        var encontrado = service.findByCpf("529.982.247-25");
        assertEquals(10L, encontrado.id());

        service.delete(10L);
        assertSame(existente, excluido);
        assertSame(existente.getPessoa(), pessoaExcluida);
    }

    @Test
    void deveAtualizarSubstituindoTelefones() {
        existente = Entidades.comId(new Cliente(), 3L);
        existente.setPessoa(Entidades.comId(new Pessoa(), 8L));
        service.create(dto(LocalDate.of(1995, 5, 10), "1721000", "Palmas"));
        existente.getPessoa().setTelefones(persistido.getPessoa().getTelefones());

        var resposta = service.update(3L, dto(LocalDate.of(1995, 5, 10), "1721000", "Palmas"));
        assertEquals(1, resposta.telefones().size());
        assertEquals(3L, resposta.id());
    }

    private ClienteRequestDTO dto(LocalDate nascimento, String ibge, String cidade) {
        return new ClienteRequestDTO(
                " Ana Martins ",
                " ANA@fitwork.com ",
                "52998224725",
                nascimento,
                "Feminino",
                List.of(new TelefoneRequestDTO("63", "999991111", true)),
                new EnderecoRequestDTO("77001002", "Quadra 101 Sul", "10", "  ", "Plano Diretor Sul", cidade, "to",
                        ibge),
                true,
                true);
    }
}
