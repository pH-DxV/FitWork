package br.unitins.topicos1.fitwork.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoProteina {
    CONCENTRADO(1L, "Concentrado"),
    ISOLADO(2L, "Isolado"),
    HIDROLISADO(3L, "Hidrolisado"),
    BLEND(4L, "Blend"),
    VEGETAL(5L, "Vegetal");

    private final Long id;
    private final String nome;

    TipoProteina(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static TipoProteina valueOf(Long id) {
        if (id == null) {
            return null;
        }
        for (TipoProteina tipo : TipoProteina.values()) {
            if (Objects.equals(tipo.getId(), id)) {
                return tipo;
            }
        }
        return null;
    }
}
