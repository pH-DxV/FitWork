package br.unitins.topicos1.aurafit.repository;

import br.unitins.topicos1.aurafit.model.Estado;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EstadoRepository implements PanacheRepository<Estado> {

    public Estado findBySigla(String sigla) {
        if (sigla == null || sigla.isBlank()) {
            return null;
        }
        return find("upper(sigla) = upper(?1)", sigla.trim()).firstResult();
    }

    public PanacheQuery<Estado> findByNome(String nome) {
        String termo = nome == null ? "" : nome.trim();
        return find("lower(nome) like lower(?1) order by nome", "%" + termo + "%");
    }

    @Override
    public PanacheQuery<Estado> findAll() {
        return find("order by nome");
    }
}
