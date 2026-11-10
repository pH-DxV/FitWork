package br.unitins.topicos1.aurafit.repository;

import br.unitins.topicos1.aurafit.model.Pessoa;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PessoaRepository implements PanacheRepository<Pessoa> {

    public Pessoa findByCpf(String cpf) {
        return find("cpf", cpf).firstResult();
    }

    public Pessoa findByCpfExceptId(String cpf, Long idPessoa) {
        if (idPessoa == null) {
            return findByCpf(cpf);
        }
        return find("cpf = ?1 and id <> ?2", cpf, idPessoa).firstResult();
    }

    public Pessoa findByEmailExceptId(String email, Long idPessoa) {
        if (idPessoa == null) {
            return find("lower(email) = lower(?1)", email.trim()).firstResult();
        }
        return find("lower(email) = lower(?1) and id <> ?2", email.trim(), idPessoa).firstResult();
    }
}
