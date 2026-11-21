package br.unitins.topicos1.fitwork.resource;

import java.io.IOException;
import java.nio.file.Files;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.dto.ProdutoRequestDTO;
import br.unitins.topicos1.fitwork.dto.ProdutoResponseDTO;
import br.unitins.topicos1.fitwork.exception.ValidationException;
import br.unitins.topicos1.fitwork.service.ArquivoArmazenado;
import br.unitins.topicos1.fitwork.service.ProdutoService;
import br.unitins.topicos1.fitwork.service.StorageService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    @Inject
    ProdutoService service;

    /**
     * Listagem paginada com filtros opcionais.
     * Ex.: GET /produtos?nome=whey&idTipoProteina=2&idCategoria=1&idMarca=3&ativo=true&page=0&pageSize=10
     */
    @GET
    public PageResponse<ProdutoResponseDTO> buscarTodos(@QueryParam("nome") String nome,
            @QueryParam("idTipoProteina") Long idTipoProteina,
            @QueryParam("idCategoria") Long idCategoria,
            @QueryParam("idMarca") Long idMarca,
            @QueryParam("ativo") Boolean ativo,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return service.buscar(nome, idTipoProteina, idCategoria, idMarca, ativo, page, pageSize);
    }

    @GET
    @Path("/{id}")
    public ProdutoResponseDTO buscarPorId(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id) {
        return service.findById(id);
    }

    @POST
    public Response incluir(
            @NotNull(message = "Os dados devem ser informados.") @Valid ProdutoRequestDTO dto,
            @Context UriInfo uriInfo) {
        ProdutoResponseDTO criado = service.create(dto);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(criado.id())).build())
                .entity(criado)
                .build();
    }

    @PUT
    @Path("/{id}")
    public ProdutoResponseDTO alterar(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id,
            @NotNull(message = "Os dados devem ser informados.") @Valid ProdutoRequestDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response apagar(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    /**
     * Upload ou substituição da imagem do produto no Object Storage S3 (RustFS).
     */
    @PATCH
    @Path("/{id}/imagem")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ProdutoResponseDTO uploadImagem(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id,
            @RestForm("arquivo") FileUpload arquivo) {
        if (arquivo == null || arquivo.uploadedFile() == null) {
            throw ValidationException.of("arquivo", "O arquivo de imagem deve ser enviado.");
        }
        if (arquivo.size() <= 0) {
            throw ValidationException.of("arquivo", "O arquivo de imagem não pode estar vazio.");
        }
        if (arquivo.size() > StorageService.TAMANHO_MAXIMO_BYTES) {
            throw ValidationException.of("arquivo", "A imagem deve ter no máximo 5 MB.");
        }
        try {
            byte[] dados = Files.readAllBytes(arquivo.uploadedFile());
            return service.salvarImagem(id, arquivo.fileName(), arquivo.contentType(), dados);
        } catch (IOException e) {
            throw ValidationException.of("arquivo", "Não foi possível ler o arquivo enviado.");
        }
    }

    /**
     * Visualização (inline) ou download (attachment quando ?download=true) da imagem do produto.
     */
    @GET
    @Path("/{id}/imagem")
    @Produces({ "image/jpeg", "image/png", "image/webp", MediaType.APPLICATION_OCTET_STREAM })
    public Response obterImagem(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id,
            @QueryParam("download") @DefaultValue("false") boolean download) {
        ArquivoArmazenado imagem = service.obterImagem(id);
        String disposicao = (download ? "attachment" : "inline")
                + "; filename=\"" + imagem.nomeArquivo().replace("\"", "") + "\"";
        return Response.ok(imagem.conteudo(), imagem.contentType())
                .header("Content-Disposition", disposicao)
                .header("Cache-Control", "no-cache")
                .build();
    }

    /**
     * Remoção da imagem do produto do Object Storage S3 (RustFS).
     */
    @DELETE
    @Path("/{id}/imagem")
    public Response removerImagem(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id) {
        service.removerImagem(id);
        return Response.noContent().build();
    }
}
