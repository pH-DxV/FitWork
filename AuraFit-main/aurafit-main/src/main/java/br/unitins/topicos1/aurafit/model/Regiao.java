package br.unitins.topicos1.aurafit.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Regiao {
    CENTRO_OESTE(1L, "Centro-Oeste"),
    NORTE(2L, "Norte"),
    NORDESTE(3L, "Nordeste"),
    SUDESTE(4L, "Sudeste"),
    SUL(5L, "Sul");

    private final Long id;
    private final String nome;

    Regiao(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static Regiao valueOf(Long id) {
        if (id == null) {
            return null;
        }
        for (Regiao regiao : Regiao.values()) {
            if (Objects.equals(regiao.getId(), id)) {
                return regiao;
            }
        }
        return null;
    }
}
