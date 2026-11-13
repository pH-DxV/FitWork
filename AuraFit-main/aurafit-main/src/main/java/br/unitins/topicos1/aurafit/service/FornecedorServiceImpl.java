package br.unitins.topicos1.aurafit.service;

import br.unitins.topicos1.aurafit.dto.FornecedorRequestDTO;
import br.unitins.topicos1.aurafit.dto.FornecedorResponseDTO;
import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.exception.ValidationException;
import br.unitins.topicos1.aurafit.mapper.FornecedorMapper;
import br.unitins.topicos1.aurafit.model.Fornecedor;
import br.unitins.topicos1.aurafit.repository.FornecedorRepository;
import br.unitins.topicos1.aurafit.repository.ProdutoRepository;
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
        return Paginacao.executar(() -> fornecedorRepository.findAll(), page, pageSize,
                FornecedorMapper::toResponse);
    }

    @Override
    public PageResponse<FornecedorResponseDTO> findByNome(String nome, int page, int pageSize) {
        return Paginacao.executar(() -> fornecedorRepository.findByNome(nome), page, pageSize,
                FornecedorMapper::toResponse);
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
            throw ValidationException.conflict("id",
                    "Este fornecedor possui produtos cadastrados e não pode ser excluído.");
        }
        fornecedorRepository.delete(fornecedor);
    }

    private void validarDados(FornecedorRequestDTO dto, Long id) {
        Textos.validarTamanhoUtil(dto.razaoSocial(), "razaoSocial", 2, 120,
                "A razão social deve conter entre 2 e 120 caracteres.");
        if (fornecedorRepository.findByCnpjExceptId(dto.cnpj(), id) != null) {
            throw ValidationException.conflict("cnpj", "Já existe um fornecedor cadastrado com esse CNPJ.");
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
