package br.unitins.topicos1.aurafit.resource;

import java.net.URI;

import io.quarkus.runtime.LaunchMode;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class RootResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response root() {
        if (LaunchMode.current() == LaunchMode.DEVELOPMENT) {
            return Response.seeOther(URI.create("/q/swagger-ui")).build();
        }
        return Response.ok("AuraFit API em execução.").build();
    }
}
