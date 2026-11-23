package br.unitins.topicos1.fitwork.resource;

import java.net.URI;

import io.quarkus.runtime.LaunchMode;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@PermitAll
public class RootResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response root() {
        if (LaunchMode.current() == LaunchMode.DEVELOPMENT) {
            return Response.seeOther(URI.create("/q/swagger-ui")).build();
        }
        return Response.ok("FitWork API em execução.").build();
    }
}
