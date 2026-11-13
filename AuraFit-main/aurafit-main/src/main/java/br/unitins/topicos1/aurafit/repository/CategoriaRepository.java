package br.unitins.topicos1.aurafit.repository;

import br.unitins.topicos1.aurafit.model.Categoria;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CategoriaRepository implements PanacheRepository<Categoria> {

    public PanacheQuery<Categoria> findByNome(String nome) {
        String termo = nome == null ? "" : nome.trim();
        return find("lower(nome) like lower(?1) order by nome", "%" + termo + "%");
    }

    public Categoria findByNomeExceptId(String nome, Long id) {
        if (id == null) {
            return find("lower(nome) = lower(?1)", nome.trim()).firstResult();
        }
        return find("lower(nome) = lower(?1) and id <> ?2", nome.trim(), id).firstResult();
    }

    @Override
    public PanacheQuery<Categoria> findAll() {
        return find("order by nome");
    }
}
