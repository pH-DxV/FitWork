package br.unitins.topicos1.fitwork.resource;

import br.unitins.topicos1.fitwork.dto.FornecedorRequestDTO;
import br.unitins.topicos1.fitwork.dto.FornecedorResponseDTO;
import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.service.FornecedorService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * Dados corporativos de fornecedores (CNPJ, contatos e endereço fiscal) são restritos ao backoffice.
 */
@Path("fornecedores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class FornecedorResource {

    @Inject
    FornecedorService service;

    @GET
    public PageResponse<FornecedorResponseDTO> buscarTodos(@QueryParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        if (nome != null && !nome.isBlank()) {
            return service.findByNome(nome, page, pageSize);
        }
        return service.findAll(page, pageSize);
    }

    @Deprecated
    @GET
    @Path("/nome/{nome}")
    public PageResponse<FornecedorResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return service.findByNome(nome, page, pageSize);
    }

    @GET
    @Path("/{id}")
    public FornecedorResponseDTO buscarPorId(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id) {
        return service.findById(id);
    }

    @POST
    public Response incluir(
            @NotNull(message = "Os dados devem ser informados.") @Valid FornecedorRequestDTO dto,
            @Context UriInfo uriInfo) {
        FornecedorResponseDTO criado = service.create(dto);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(criado.id())).build())
                .entity(criado)
                .build();
    }

    @PUT
    @Path("/{id}")
    public FornecedorResponseDTO alterar(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id,
            @NotNull(message = "Os dados devem ser informados.") @Valid FornecedorRequestDTO dto) {
        return service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response apagar(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
