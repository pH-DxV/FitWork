package br.unitins.topicos1.fitwork.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Cliente extends DefaultEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "id_pessoa", nullable = false, unique = true)
    private Pessoa pessoa;

    // aceita receber novidades e promoções da loja
    @Column(nullable = false)
    private boolean aceitaNovidades;

    @Column(nullable = false)
    private boolean ativo = true;

    /**
     * Identificador (claim "sub") da conta do usuário no Keycloak.
     * A senha fica exclusivamente no Keycloak: o banco do e-commerce NUNCA armazena senhas.
     * Pode ser nulo para clientes cadastrados pelo administrador que ainda não possuem acesso.
     */
    @Column(length = 64, unique = true)
    private String keycloakId;

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    public boolean isAceitaNovidades() {
        return aceitaNovidades;
    }

    public void setAceitaNovidades(boolean aceitaNovidades) {
        this.aceitaNovidades = aceitaNovidades;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
