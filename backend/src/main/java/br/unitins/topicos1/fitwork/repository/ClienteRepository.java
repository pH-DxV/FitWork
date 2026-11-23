package br.unitins.topicos1.fitwork.repository;

import br.unitins.topicos1.fitwork.model.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {

    public Cliente findByCpf(String cpf) {
        return find("pessoa.cpf", cpf).firstResult();
    }

    public Cliente findByKeycloakId(String keycloakId) {
        if (keycloakId == null || keycloakId.isBlank()) {
            return null;
        }
        return find("keycloakId", keycloakId).firstResult();
    }

    /** Cliente com o e-mail informado que ainda NÃO está vinculado a nenhuma conta do Keycloak. */
    public Cliente findNaoVinculadoByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return find("lower(pessoa.email) = lower(?1) and keycloakId is null", email.trim()).firstResult();
    }

    public PanacheQuery<Cliente> findByNome(String nome) {
        String termo = nome == null ? "" : nome.trim();
        return find("lower(pessoa.nome) like lower(?1) order by pessoa.nome", "%" + termo + "%");
    }

    @Override
    public PanacheQuery<Cliente> findAll() {
        return find("order by pessoa.nome");
    }
}
