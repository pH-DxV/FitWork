package br.unitins.topicos1.fitwork.resource;

import br.unitins.topicos1.fitwork.dto.EstadoResponseDTO;
import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.service.EstadoService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("estados")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class EstadoResource {

    @Inject
    EstadoService service;

    @GET
    public PageResponse<EstadoResponseDTO> buscarTodos(@QueryParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("30") int pageSize) {
        if (nome != null && !nome.isBlank()) {
            return service.findByNome(nome, page, pageSize);
        }
        return service.findAll(page, pageSize);
    }

    @Deprecated
    @GET
    @Path("/nome/{nome}")
    public PageResponse<EstadoResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return service.findByNome(nome, page, pageSize);
    }

    @GET
    @Path("/{id}")
    public EstadoResponseDTO buscarPorId(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id) {
        return service.findById(id);
    }

    @GET
    @Path("/sigla/{sigla}")
    public EstadoResponseDTO buscarPorSigla(@PathParam("sigla") String sigla) {
        return service.findBySigla(sigla);
    }
}
