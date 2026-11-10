package br.unitins.topicos1.aurafit.repository;

import br.unitins.topicos1.aurafit.model.Sabor;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SaborRepository implements PanacheRepository<Sabor> {

    public PanacheQuery<Sabor> findByNome(String nome) {
        return find("lower(nome) like lower(?1) order by nome", "%" + nome.trim() + "%");
    }

    public Sabor findByNomeExceptId(String nome, Long id) {
        if (id == null) {
            return find("lower(nome) = lower(?1)", nome.trim()).firstResult();
        }
        return find("lower(nome) = lower(?1) and id <> ?2", nome.trim(), id).firstResult();
    }

    @Override
    public PanacheQuery<Sabor> findAll() {
        return find("order by nome");
    }
}
