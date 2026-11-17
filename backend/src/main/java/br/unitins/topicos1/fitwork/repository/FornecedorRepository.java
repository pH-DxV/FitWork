package br.unitins.topicos1.fitwork.repository;

import br.unitins.topicos1.fitwork.model.Fornecedor;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FornecedorRepository implements PanacheRepository<Fornecedor> {

    public PanacheQuery<Fornecedor> findByNome(String razaoSocial) {
        String termo = razaoSocial == null ? "" : razaoSocial.trim();
        return find("lower(razaoSocial) like lower(?1) order by razaoSocial", "%" + termo + "%");
    }

    public Fornecedor findByCnpjExceptId(String cnpj, Long id) {
        if (id == null) {
            return find("cnpj", cnpj).firstResult();
        }
        return find("cnpj = ?1 and id <> ?2", cnpj, id).firstResult();
    }

    @Override
    public PanacheQuery<Fornecedor> findAll() {
        return find("order by razaoSocial");
    }
}
