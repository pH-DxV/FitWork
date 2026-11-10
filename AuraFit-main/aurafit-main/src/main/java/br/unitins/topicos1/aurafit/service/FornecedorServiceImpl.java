package br.unitins.topicos1.aurafit.service;

import java.util.function.Supplier;
import br.unitins.topicos1.aurafit.dto.FornecedorRequestDTO;
import br.unitins.topicos1.aurafit.dto.FornecedorResponseDTO;
import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.exception.ValidationException;
import br.unitins.topicos1.aurafit.mapper.FornecedorMapper;
import br.unitins.topicos1.aurafit.model.Fornecedor;
import br.unitins.topicos1.aurafit.repository.FornecedorRepository;
import br.unitins.topicos1.aurafit.repository.ProdutoRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FornecedorServiceImpl implements FornecedorService {

    @Inject
    FornecedorRepository fornecedorRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Override
    public PageResponse<FornecedorResponseDTO> findAll(int page, int pageSize) {
        return paginar(() -> fornecedorRepository.findAll(), page, pageSize);
    }

    @Override
    public PageResponse<FornecedorResponseDTO> findByNome(String nome, int page, int pageSize) {
        return paginar(() -> fornecedorRepository.findByNome(nome), page, pageSize);
    }

    @Override
    public FornecedorResponseDTO findById(long id) {
        return FornecedorMapper.toResponse(buscarFornecedorOuFalhar(id));
    }

    @Override
    @Transactional
    public FornecedorResponseDTO create(FornecedorRequestDTO dto) {
        validarDados(dto, null);
        Fornecedor fornecedor = new Fornecedor();
        aplicarDados(fornecedor, dto);
        fornecedorRepository.persist(fornecedor);
        return FornecedorMapper.toResponse(fornecedor);
    }

    @Override
    @Transactional
    public FornecedorResponseDTO update(long id, FornecedorRequestDTO dto) {
        Fornecedor fornecedor = buscarFornecedorOuFalhar(id);
        validarDados(dto, id);
        aplicarDados(fornecedor, dto);
        return FornecedorMapper.toResponse(fornecedor);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Fornecedor fornecedor = buscarFornecedorOuFalhar(id);
        if (produtoRepository.existeComFornecedor(id)) {
            throw ValidationException.of("id", "Este fornecedor possui produtos cadastrados e não pode ser excluído.");
        }
        fornecedorRepository.delete(fornecedor);
    }

    private PageResponse<FornecedorResponseDTO> paginar(Supplier<PanacheQuery<Fornecedor>> consulta, int page, int pageSize) {
        Paginacao.validar(page, pageSize);
        PanacheQuery<Fornecedor> query = consulta.get();
        return PageResponse.of(query.page(page, pageSize).list(), page, pageSize, query.count(),
                FornecedorMapper::toResponse);
    }

    private void validarDados(FornecedorRequestDTO dto, Long id) {
        if (fornecedorRepository.findByCnpjExceptId(dto.cnpj(), id) != null) {
            throw ValidationException.of("cnpj", "Já existe um fornecedor cadastrado com esse CNPJ.");
        }
    }

    private void aplicarDados(Fornecedor fornecedor, FornecedorRequestDTO dto) {
        fornecedor.setRazaoSocial(dto.razaoSocial().trim());
        fornecedor.setCnpj(dto.cnpj());
        fornecedor.setTelefone(dto.telefone());
        String email = Textos.limparOuNulo(dto.email());
        fornecedor.setEmail(email == null ? null : email.toLowerCase());
    }

    private Fornecedor buscarFornecedorOuFalhar(long id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id);
        if (fornecedor == null) {
            throw new ResourceNotFoundException("Fornecedor não encontrado.");
        }
        return fornecedor;
    }
}
