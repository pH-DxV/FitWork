package br.unitins.topicos1.aurafit.model;

/**
 * Utilitário de testes: atribui id às entidades (o setter é protegido em DefaultEntity).
 */
public final class Entidades {

    private Entidades() {
    }

    public static <T extends DefaultEntity> T comId(T entidade, long id) {
        entidade.setId(id);
        return entidade;
    }
}
