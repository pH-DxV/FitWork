package br.unitins.topicos1.fitwork.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Fornecedor extends DefaultEntity {

    @Column(length = 120, nullable = false)
    private String razaoSocial;

    // apenas os 14 dígitos, sem máscara
    @Column(length = 14, nullable = false, unique = true)
    private String cnpj;

    // DDD + número, apenas dígitos
    @Column(length = 11, nullable = false)
    private String telefone;

    @Column(length = 100)
    private String email;

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
