package br.unitins.topicos1.aurafit.resource;

import br.unitins.topicos1.aurafit.dto.SaborRequestDTO;
import br.unitins.topicos1.aurafit.dto.SaborResponseDTO;
import br.unitins.topicos1.aurafit.dto.PageResponse;
import br.unitins.topicos1.aurafit.service.SaborService;
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

@Path("sabores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SaborResource {

    @Inject
    SaborService service;

    @GET
    public PageResponse<SaborResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return service.findAll(page, pageSize);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<SaborResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return service.findByNome(nome, page, pageSize);
    }

    @GET
    @Path("/{id}")
    public SaborResponseDTO buscarPorId(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    public Response incluir(SaborRequestDTO dto) {
        return Response.status(Response.Status.CREATED).entity(service.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public SaborResponseDTO alterar(@PathParam("id") Long id, SaborRequestDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response apagar(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
