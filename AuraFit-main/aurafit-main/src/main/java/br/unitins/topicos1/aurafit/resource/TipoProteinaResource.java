package br.unitins.topicos1.aurafit.resource;

import br.unitins.topicos1.aurafit.exception.ResourceNotFoundException;
import br.unitins.topicos1.aurafit.model.TipoProteina;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("tipos-proteina")
@Produces(MediaType.APPLICATION_JSON)
public class TipoProteinaResource {

    @GET
    public TipoProteina[] buscarTodos() {
        return TipoProteina.values();
    }

    @GET
    @Path("/{id}")
    public TipoProteina buscarPorId(@PathParam("id") Long id) {
        TipoProteina tipo = TipoProteina.valueOf(id);
        if (tipo == null) {
            throw new ResourceNotFoundException("Tipo de proteína não encontrado.");
        }
        return tipo;
    }
}
