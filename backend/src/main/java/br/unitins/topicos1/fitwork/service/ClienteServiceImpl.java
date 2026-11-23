package br.unitins.topicos1.fitwork.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

import br.unitins.topicos1.fitwork.dto.ClienteRequestDTO;
import br.unitins.topicos1.fitwork.dto.ClienteResponseDTO;
import br.unitins.topicos1.fitwork.dto.EnderecoRequestDTO;
import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.exception.ResourceNotFoundException;
import br.unitins.topicos1.fitwork.exception.ValidationException;
import br.unitins.topicos1.fitwork.mapper.ClienteMapper;
import br.unitins.topicos1.fitwork.mapper.TelefoneMapper;
import br.unitins.topicos1.fitwork.model.Cliente;
import br.unitins.topicos1.fitwork.model.Endereco;
import br.unitins.topicos1.fitwork.model.Pessoa;
import br.unitins.topicos1.fitwork.repository.ClienteRepository;
import br.unitins.topicos1.fitwork.repository.PessoaRepository;
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

    @Inject
    KeycloakGateway keycloakGateway;

    @Override
    @Transactional
    public PageResponse<ClienteResponseDTO> findAll(int page, int pageSize) {
        return Paginacao.executar(() -> clienteRepository.findAll(), page, pageSize, ClienteMapper::toResponse);
    }

    @Override
    @Transactional
    public PageResponse<ClienteResponseDTO> findByNome(String nome, int page, int pageSize) {
        return Paginacao.executar(() -> clienteRepository.findByNome(nome), page, pageSize, ClienteMapper::toResponse);
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
        return criarInterno(dto, null);
    }

    @Override
    @Transactional
    public void validarNovoCadastro(ClienteRequestDTO dto) {
        validarDados(dto, null);
    }

    @Override
    @Transactional
    public ClienteResponseDTO criarVinculadoAoKeycloak(ClienteRequestDTO dto, String keycloakId) {
        return criarInterno(dto, keycloakId);
    }

    @Override
    @Transactional
    public ClienteResponseDTO vincularOuBuscarPorKeycloak(String keycloakId, String email) {
        Cliente cliente = clienteRepository.findByKeycloakId(keycloakId);
        if (cliente == null && email != null && !email.isBlank()) {
            cliente = clienteRepository.findNaoVinculadoByEmail(email);
            if (cliente != null && keycloakId != null && !keycloakId.isBlank()) {
                cliente.setKeycloakId(keycloakId);
            }
        }
        if (cliente == null) {
            return null;
        }
        if (!cliente.isAtivo()) {
            throw new io.quarkus.security.ForbiddenException(
                    "Sua conta de cliente está desativada. Entre em contato com o suporte.");
        }
        return ClienteMapper.toResponse(cliente);
    }

    private ClienteResponseDTO criarInterno(ClienteRequestDTO dto, String keycloakId) {
        validarDados(dto, null);

        Cliente cliente = new Cliente();
        cliente.setPessoa(new Pessoa());
        cliente.setKeycloakId(Textos.limparOuNulo(keycloakId));
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
        if (keycloakGateway != null && cliente.getKeycloakId() != null) {
            keycloakGateway.atualizarDadosUsuario(
                    cliente.getKeycloakId(),
                    cliente.getPessoa().getNome(),
                    cliente.getPessoa().getEmail(),
                    cliente.isAtivo());
        }
        return ClienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Cliente cliente = buscarClienteOuFalhar(id);
        String keycloakId = cliente.getKeycloakId();
        Pessoa pessoa = cliente.getPessoa();
        clienteRepository.delete(cliente);
        if (pessoa != null && !pessoaPossuiOutrosVinculos(pessoa.getId())) {
            pessoaRepository.delete(pessoa);
        }
        if (keycloakGateway != null && keycloakId != null) {
            keycloakGateway.removerUsuario(keycloakId);
        }
    }

    /**
     * Verifica se a Pessoa está vinculada a outros papéis do sistema (ex.: Funcionario/Usuario).
     */
    protected boolean pessoaPossuiOutrosVinculos(Long idPessoa) {
        return false;
    }

    private void validarDados(ClienteRequestDTO dto, Long idPessoa) {
        Textos.validarTamanhoUtil(dto.nome(), "nome", 2, 120, "O nome deve conter entre 2 e 120 caracteres.");
        if (Period.between(dto.dataNascimento(), LocalDate.now()).getYears() < IDADE_MINIMA) {
            throw ValidationException.of("dataNascimento",
                    "O cliente deve ter pelo menos " + IDADE_MINIMA + " anos.");
        }
        if (pessoaRepository.findByCpfExceptId(dto.cpf(), idPessoa) != null) {
            throw ValidationException.conflict("cpf", "Já existe uma pessoa cadastrada com esse CPF.");
        }
        if (pessoaRepository.findByEmailExceptId(dto.email(), idPessoa) != null) {
            throw ValidationException.conflict("email", "Já existe uma pessoa cadastrada com esse e-mail.");
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
