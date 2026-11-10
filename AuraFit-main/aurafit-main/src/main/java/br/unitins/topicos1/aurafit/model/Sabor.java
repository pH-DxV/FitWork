package br.unitins.topicos1.aurafit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Sabor extends DefaultEntity {

    @Column(length = 80, nullable = false, unique = true)
    private String nome;

    @Column(length = 255)
    private String descricao;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
