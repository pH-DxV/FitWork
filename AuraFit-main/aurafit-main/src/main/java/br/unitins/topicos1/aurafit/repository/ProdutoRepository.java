package br.unitins.topicos1.aurafit.repository;

import java.util.HashMap;
import java.util.Map;

import br.unitins.topicos1.aurafit.model.Produto;
import br.unitins.topicos1.aurafit.model.TipoProteina;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProdutoRepository implements PanacheRepository<Produto> {

    public PanacheQuery<Produto> findByNome(String nome) {
        String termo = nome == null ? "" : nome.trim();
        return find("lower(nome) like lower(?1) order by nome", "%" + termo + "%");
    }

    /**
     * Busca com filtros opcionais (os nulos são ignorados).
     */
    public PanacheQuery<Produto> buscar(String nome, TipoProteina tipoProteina, Long idCategoria, Long idMarca) {
        return buscar(nome, tipoProteina, idCategoria, idMarca, null);
    }

    /**
     * Busca com filtros opcionais, incluindo status de ativação (os nulos são ignorados).
     */
    public PanacheQuery<Produto> buscar(String nome, TipoProteina tipoProteina, Long idCategoria, Long idMarca,
            Boolean ativo) {
        StringBuilder consulta = new StringBuilder("1 = 1");
        Map<String, Object> parametros = new HashMap<>();

        if (nome != null && !nome.isBlank()) {
            consulta.append(" and lower(nome) like lower(:nome)");
            parametros.put("nome", "%" + nome.trim() + "%");
        }
        if (tipoProteina != null) {
            consulta.append(" and tipoProteina = :tipoProteina");
            parametros.put("tipoProteina", tipoProteina);
        }
        if (idCategoria != null) {
            consulta.append(" and categoria.id = :idCategoria");
            parametros.put("idCategoria", idCategoria);
        }
        if (idMarca != null) {
            consulta.append(" and marca.id = :idMarca");
            parametros.put("idMarca", idMarca);
        }
        if (ativo != null) {
            consulta.append(" and ativo = :ativo");
            parametros.put("ativo", ativo);
        }
        consulta.append(" order by nome");

        return find(consulta.toString(), parametros);
    }

    public boolean existeComCategoria(Long idCategoria) {
        return count("categoria.id", idCategoria) > 0;
    }

    public boolean existeComMarca(Long idMarca) {
        return count("marca.id", idMarca) > 0;
    }

    public boolean existeComFornecedor(Long idFornecedor) {
        return count("fornecedor.id", idFornecedor) > 0;
    }

    public boolean existeComSabor(Long idSabor) {
        Long total = getEntityManager()
                .createQuery("select count(p) from Produto p join p.sabores s where s.id = :idSabor", Long.class)
                .setParameter("idSabor", idSabor)
                .getSingleResult();
        return total > 0;
    }

    @Override
    public PanacheQuery<Produto> findAll() {
        return find("order by nome");
    }
}
