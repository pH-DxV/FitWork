package br.unitins.topicos1.aurafit.repository;

import br.unitins.topicos1.aurafit.model.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {

    public Cliente findByCpf(String cpf) {
        return find("pessoa.cpf", cpf).firstResult();
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
