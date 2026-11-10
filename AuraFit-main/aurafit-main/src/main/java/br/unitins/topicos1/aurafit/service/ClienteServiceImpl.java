package br.unitins.topicos1.aurafit.service;

import java.util.function.Supplier;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

import br.unitins.topicos1.aurafit.dto.ClienteRequestDTO;
import br.unitins.topicos1.aurafit.dto.ClienteResponseDTO;
import br.unitins.topicos1.aurafit.dto.EnderecoRequestDTO;
import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.exception.ValidationException;
import br.unitins.topicos1.aurafit.mapper.ClienteMapper;
import br.unitins.topicos1.aurafit.mapper.TelefoneMapper;
import br.unitins.topicos1.aurafit.model.Cliente;
import br.unitins.topicos1.aurafit.model.Endereco;
import br.unitins.topicos1.aurafit.model.Pessoa;
import br.unitins.topicos1.aurafit.repository.ClienteRepository;
import br.unitins.topicos1.aurafit.repository.PessoaRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ClienteServiceImpl implements ClienteService {

    static final int IDADE_MINIMA = 16;

    @Inject
    ClienteRepository clienteRepository;

    @Inject
    PessoaRepository pessoaRepository;

    @Inject
    MunicipioService municipioService;

    @Override
    @Transactional
    public PageResponse<ClienteResponseDTO> findAll(int page, int pageSize) {
        return paginar(() -> clienteRepository.findAll(), page, pageSize);
    }

    @Override
    @Transactional
    public PageResponse<ClienteResponseDTO> findByNome(String nome, int page, int pageSize) {
        return paginar(() -> clienteRepository.findByNome(nome), page, pageSize);
    }

    @Override
    @Transactional
    public ClienteResponseDTO findById(long id) {
        return ClienteMapper.toResponse(buscarClienteOuFalhar(id));
    }

    @Override
    @Transactional
    public ClienteResponseDTO findByCpf(String cpf) {
        Cliente cliente = clienteRepository.findByCpf(Textos.somenteDigitos(cpf));
        if (cliente == null) {
            throw new ResourceNotFoundException("Cliente não encontrado.");
        }
        return ClienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO create(ClienteRequestDTO dto) {
        validarDados(dto, null);

        Cliente cliente = new Cliente();
        cliente.setPessoa(new Pessoa());
        aplicarDados(cliente, dto);

        clienteRepository.persist(cliente);
        return ClienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO update(long id, ClienteRequestDTO dto) {
        Cliente cliente = buscarClienteOuFalhar(id);
        validarDados(dto, cliente.getPessoa().getId());
        aplicarDados(cliente, dto);
        return ClienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Cliente cliente = buscarClienteOuFalhar(id);
        Pessoa pessoa = cliente.getPessoa();
        clienteRepository.delete(cliente);
        // a pessoa (com telefones e endereço) pertence apenas ao cliente
        pessoaRepository.delete(pessoa);
    }

    private PageResponse<ClienteResponseDTO> paginar(Supplier<PanacheQuery<Cliente>> consulta, int page, int pageSize) {
        Paginacao.validar(page, pageSize);
        PanacheQuery<Cliente> query = consulta.get();
        return PageResponse.of(query.page(page, pageSize).list(), page, pageSize, query.count(),
                ClienteMapper::toResponse);
    }

    private void validarDados(ClienteRequestDTO dto, Long idPessoa) {
        if (Period.between(dto.dataNascimento(), LocalDate.now()).getYears() < IDADE_MINIMA) {
            throw ValidationException.of("dataNascimento",
                    "O cliente deve ter pelo menos " + IDADE_MINIMA + " anos.");
        }
        if (pessoaRepository.findByCpfExceptId(dto.cpf(), idPessoa) != null) {
            throw ValidationException.of("cpf", "Já existe uma pessoa cadastrada com esse CPF.");
        }
        if (pessoaRepository.findByEmailExceptId(dto.email(), idPessoa) != null) {
            throw ValidationException.of("email", "Já existe uma pessoa cadastrada com esse e-mail.");
        }
    }

    private void aplicarDados(Cliente cliente, ClienteRequestDTO dto) {
        Pessoa pessoa = cliente.getPessoa();
        pessoa.setNome(dto.nome().trim());
        pessoa.setEmail(dto.email().trim().toLowerCase());
        pessoa.setCpf(dto.cpf());
        pessoa.setDataNascimento(dto.dataNascimento());
        pessoa.setGenero(Textos.limparOuNulo(dto.genero()));
        pessoa.setTelefones(new ArrayList<>(dto.telefones().stream().map(TelefoneMapper::toModel).toList()));

        Endereco endereco = pessoa.getEndereco() != null ? pessoa.getEndereco() : new Endereco();
        aplicarEndereco(endereco, dto.endereco());
        pessoa.setEndereco(endereco);

        cliente.setAceitaNovidades(dto.aceitaNovidades());
        cliente.setAtivo(dto.ativo());
    }

    private void aplicarEndereco(Endereco endereco, EnderecoRequestDTO dto) {
        endereco.setCep(dto.cep());
        endereco.setLogradouro(dto.logradouro().trim());
        endereco.setNumero(dto.numero().trim());
        endereco.setComplemento(Textos.limparOuNulo(dto.complemento()));
        endereco.setBairro(dto.bairro().trim());
        endereco.setMunicipio(municipioService.buscarOuCriar(dto.cidade(), dto.uf(), dto.codigoIbge()));
    }

    private Cliente buscarClienteOuFalhar(long id) {
        Cliente cliente = clienteRepository.findById(id);
        if (cliente == null) {
            throw new ResourceNotFoundException("Cliente não encontrado.");
        }
        return cliente;
    }
}
