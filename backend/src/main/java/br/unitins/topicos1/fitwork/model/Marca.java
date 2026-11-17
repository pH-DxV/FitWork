package br.unitins.topicos1.fitwork.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Marca extends DefaultEntity {

    @Column(length = 100, nullable = false)
    private String nome;

    // apenas os 14 dígitos, sem máscara
    @Column(length = 14, nullable = false, unique = true)
    private String cnpj;

    @Column(length = 100)
    private String emailContato;

    @Column(nullable = false)
    private boolean ativa = true;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEmailContato() {
        return emailContato;
    }

    public void setEmailContato(String emailContato) {
        this.emailContato = emailContato;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}
