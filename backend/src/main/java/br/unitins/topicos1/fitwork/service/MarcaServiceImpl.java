package br.unitins.topicos1.fitwork.service;

import br.unitins.topicos1.fitwork.dto.MarcaRequestDTO;
import br.unitins.topicos1.fitwork.dto.MarcaResponseDTO;
import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.exception.ResourceNotFoundException;
import br.unitins.topicos1.fitwork.exception.ValidationException;
import br.unitins.topicos1.fitwork.mapper.MarcaMapper;
import br.unitins.topicos1.fitwork.model.Marca;
import br.unitins.topicos1.fitwork.repository.MarcaRepository;
import br.unitins.topicos1.fitwork.repository.ProdutoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MarcaServiceImpl implements MarcaService {

    @Inject
    MarcaRepository marcaRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Override
    public PageResponse<MarcaResponseDTO> findAll(int page, int pageSize) {
        return Paginacao.executar(() -> marcaRepository.findAll(), page, pageSize, MarcaMapper::toResponse);
    }

    @Override
    public PageResponse<MarcaResponseDTO> findByNome(String nome, int page, int pageSize) {
        return Paginacao.executar(() -> marcaRepository.findByNome(nome), page, pageSize, MarcaMapper::toResponse);
    }

    @Override
    public MarcaResponseDTO findById(long id) {
        return MarcaMapper.toResponse(buscarMarcaOuFalhar(id));
    }

    @Override
    @Transactional
    public MarcaResponseDTO create(MarcaRequestDTO dto) {
        validarDados(dto, null);
        Marca marca = new Marca();
        aplicarDados(marca, dto);
        marcaRepository.persist(marca);
        return MarcaMapper.toResponse(marca);
    }

    @Override
    @Transactional
    public MarcaResponseDTO update(long id, MarcaRequestDTO dto) {
        Marca marca = buscarMarcaOuFalhar(id);
        validarDados(dto, id);
        aplicarDados(marca, dto);
        return MarcaMapper.toResponse(marca);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Marca marca = buscarMarcaOuFalhar(id);
        if (produtoRepository.existeComMarca(id)) {
            throw ValidationException.conflict("id",
                    "Esta marca possui produtos cadastrados e não pode ser excluída. Você pode inativá-la.");
        }
        marcaRepository.delete(marca);
    }

    private void validarDados(MarcaRequestDTO dto, Long id) {
        Textos.validarTamanhoUtil(dto.nome(), "nome", 2, 100, "O nome deve conter entre 2 e 100 caracteres.");
        if (marcaRepository.findByCnpjExceptId(dto.cnpj(), id) != null) {
            throw ValidationException.conflict("cnpj", "Já existe uma marca cadastrada com esse CNPJ.");
        }
    }

    private void aplicarDados(Marca marca, MarcaRequestDTO dto) {
        marca.setNome(dto.nome().trim());
        marca.setCnpj(dto.cnpj());
        String email = Textos.limparOuNulo(dto.emailContato());
        marca.setEmailContato(email == null ? null : email.toLowerCase());
        marca.setAtiva(dto.ativa());
    }

    private Marca buscarMarcaOuFalhar(long id) {
        Marca marca = marcaRepository.findById(id);
        if (marca == null) {
            throw new ResourceNotFoundException("Marca não encontrada.");
        }
        return marca;
    }
}
