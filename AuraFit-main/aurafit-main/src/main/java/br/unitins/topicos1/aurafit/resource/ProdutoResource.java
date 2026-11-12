package br.unitins.topicos1.aurafit.resource;

import br.unitins.topicos1.aurafit.dto.ProdutoRequestDTO;
import br.unitins.topicos1.aurafit.dto.ProdutoResponseDTO;
import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.service.ProdutoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
    @Path("/nome/{nome}")
    public PageResponse<ProdutoResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return service.findByNome(nome, page, pageSize);
    }

    @GET
    @Path("/{id}")
    public ProdutoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    public Response incluir(ProdutoRequestDTO dto) {
        return Response.status(Response.Status.CREATED).entity(service.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public ProdutoResponseDTO alterar(@PathParam("id") Long id, ProdutoRequestDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response apagar(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
