package br.unitins.topicos1.aurafit.repository;

import br.unitins.topicos1.aurafit.model.Municipio;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MunicipioRepository implements PanacheRepository<Municipio> {

    public PanacheQuery<Municipio> findByNome(String nome) {
        return find("lower(nome) like lower(?1) order by nome", "%" + nome.trim() + "%");
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
