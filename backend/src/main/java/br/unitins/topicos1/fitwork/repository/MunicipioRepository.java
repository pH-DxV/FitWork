package br.unitins.topicos1.fitwork.repository;

import br.unitins.topicos1.fitwork.model.Municipio;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MunicipioRepository implements PanacheRepository<Municipio> {

    public PanacheQuery<Municipio> findByNome(String nome) {
        String termo = nome == null ? "" : nome.trim();
        return find("lower(nome) like lower(?1) order by nome", "%" + termo + "%");
    }

    public Municipio findByCodigoIbge(String codigoIbge) {
        return find("codigoIbge", codigoIbge).firstResult();
    }

    public Municipio findByNomeEEstado(String nome, Long idEstado) {
        return find("lower(nome) = lower(?1) and estado.id = ?2", nome.trim(), idEstado).firstResult();
    }

    @Override
    public PanacheQuery<Municipio> findAll() {
        return find("order by nome");
    }
}
