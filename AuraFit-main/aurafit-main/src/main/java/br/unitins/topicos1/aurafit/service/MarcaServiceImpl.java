package br.unitins.topicos1.aurafit.service;

import java.util.function.Supplier;
import br.unitins.topicos1.aurafit.dto.MarcaRequestDTO;
import br.unitins.topicos1.aurafit.dto.MarcaResponseDTO;
import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.exception.ValidationException;
import br.unitins.topicos1.aurafit.mapper.MarcaMapper;
import br.unitins.topicos1.aurafit.model.Marca;
import br.unitins.topicos1.aurafit.repository.MarcaRepository;
import br.unitins.topicos1.aurafit.repository.ProdutoRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
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
        return paginar(() -> marcaRepository.findAll(), page, pageSize);
    }

    @Override
    public PageResponse<MarcaResponseDTO> findByNome(String nome, int page, int pageSize) {
        return paginar(() -> marcaRepository.findByNome(nome), page, pageSize);
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
            throw ValidationException.of("id",
                    "Esta marca possui produtos cadastrados e não pode ser excluída. Você pode inativá-la.");
        }
        marcaRepository.delete(marca);
    }

    private PageResponse<MarcaResponseDTO> paginar(Supplier<PanacheQuery<Marca>> consulta, int page, int pageSize) {
        Paginacao.validar(page, pageSize);
        PanacheQuery<Marca> query = consulta.get();
        return PageResponse.of(query.page(page, pageSize).list(), page, pageSize, query.count(),
                MarcaMapper::toResponse);
    }

    private void validarDados(MarcaRequestDTO dto, Long id) {
        if (marcaRepository.findByCnpjExceptId(dto.cnpj(), id) != null) {
            throw ValidationException.of("cnpj", "Já existe uma marca cadastrada com esse CNPJ.");
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
