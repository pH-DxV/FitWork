package br.unitins.topicos1.aurafit.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import br.unitins.topicos1.aurafit.model.Sabor;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SaborRepository implements PanacheRepository<Sabor> {

    public PanacheQuery<Sabor> findByNome(String nome) {
        String termo = nome == null ? "" : nome.trim();
        return find("lower(nome) like lower(?1) order by nome", "%" + termo + "%");
    }

    public Sabor findByNomeExceptId(String nome, Long id) {
        if (id == null) {
            return find("lower(nome) = lower(?1)", nome.trim()).firstResult();
        }
        return find("lower(nome) = lower(?1) and id <> ?2", nome.trim(), id).firstResult();
    }

    public List<Sabor> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return list("id in ?1", ids);
    }

    @Override
    public PanacheQuery<Sabor> findAll() {
        return find("order by nome");
    }
}
