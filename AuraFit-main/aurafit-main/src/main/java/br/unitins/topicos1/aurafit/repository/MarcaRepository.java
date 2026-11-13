package br.unitins.topicos1.aurafit.repository;

import br.unitins.topicos1.aurafit.model.Marca;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MarcaRepository implements PanacheRepository<Marca> {

    public PanacheQuery<Marca> findByNome(String nome) {
        String termo = nome == null ? "" : nome.trim();
        return find("lower(nome) like lower(?1) order by nome", "%" + termo + "%");
    }

    public Marca findByCnpjExceptId(String cnpj, Long id) {
        if (id == null) {
            return find("cnpj", cnpj).firstResult();
        }
        return find("cnpj = ?1 and id <> ?2", cnpj, id).firstResult();
    }

    @Override
    public PanacheQuery<Marca> findAll() {
        return find("order by nome");
    }
}
