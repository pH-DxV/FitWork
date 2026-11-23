package br.unitins.topicos1.fitwork.resource;

import br.unitins.topicos1.fitwork.dto.ClienteRequestDTO;
import br.unitins.topicos1.fitwork.dto.ClienteResponseDTO;
import br.unitins.topicos1.fitwork.dto.PageResponse;
import br.unitins.topicos1.fitwork.dto.UsuarioAutenticadoDTO;
import br.unitins.topicos1.fitwork.exception.ResourceNotFoundException;
import br.unitins.topicos1.fitwork.service.AuthService;
import br.unitins.topicos1.fitwork.service.ClienteService;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
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
 * Endpoints de clientes protegidos por papéis (RBAC) e verificação de titularidade (anti-IDOR).
 * Dados pessoais (CPF, telefone, endereço) nunca ficam expostos publicamente.
 */
@Path("clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    @Inject
    ClienteService service;

    @Inject
    AuthService authService;

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @RolesAllowed("ADMIN")
    public PageResponse<ClienteResponseDTO> buscarTodos(@QueryParam("nome") String nome,
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
    @RolesAllowed("ADMIN")
    public PageResponse<ClienteResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        return service.findByNome(nome, page, pageSize);
    }

    @GET
    @Path("/me")
    @RolesAllowed({ "CLIENTE", "ADMIN" })
    public ClienteResponseDTO buscarMeuCadastro() {
        UsuarioAutenticadoDTO usuario = authService.obterUsuarioAtual(securityIdentity);
        if (usuario.cliente() == null) {
            throw new ResourceNotFoundException("Nenhum cadastro de cliente vinculado a este usuário.");
        }
        return usuario.cliente();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "CLIENTE", "ADMIN" })
    public ClienteResponseDTO buscarPorId(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id) {
        exigirTitularidadeOuAdmin(id);
        return service.findById(id);
    }

    @GET
    @Path("/por-cpf")
    @RolesAllowed("ADMIN")
    public ClienteResponseDTO buscarPorCpf(@QueryParam("cpf") String cpf) {
        return service.findByCpf(cpf);
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response incluir(
            @NotNull(message = "Os dados devem ser informados.") @Valid ClienteRequestDTO dto,
            @Context UriInfo uriInfo) {
        ClienteResponseDTO criado = service.create(dto);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(criado.id())).build())
                .entity(criado)
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "CLIENTE", "ADMIN" })
    public ClienteResponseDTO alterar(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id,
            @NotNull(message = "Os dados devem ser informados.") @Valid ClienteRequestDTO dto) {
        exigirTitularidadeOuAdmin(id);
        ClienteRequestDTO dadosNormalizados = securityIdentity.hasRole("ADMIN")
                ? dto
                : new ClienteRequestDTO(
                        dto.nome(),
                        dto.email(),
                        dto.cpf(),
                        dto.dataNascimento(),
                        dto.genero(),
                        dto.telefones(),
                        dto.endereco(),
                        dto.aceitaNovidades(),
                        true);
        return service.update(id, dadosNormalizados);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response apagar(
            @PathParam("id") @Positive(message = "O id deve ser positivo.") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    /**
     * Prevenção contra IDOR (Insecure Direct Object Reference):
     * um usuário com papel CLIENTE só pode consultar ou alterar o seu próprio registro.
     */
    private void exigirTitularidadeOuAdmin(Long idAlvo) {
        if (securityIdentity.hasRole("ADMIN")) {
            return;
        }
        UsuarioAutenticadoDTO usuario = authService.obterUsuarioAtual(securityIdentity);
        if (usuario.cliente() == null || !idAlvo.equals(usuario.cliente().id())) {
            throw new ForbiddenException("Você só tem permissão para acessar ou alterar o seu próprio cadastro.");
        }
    }
}
