package br.unitins.topicos1.fitwork.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.BatchSize;

@Entity
public class Produto extends DefaultEntity {

    @Column(length = 120, nullable = false)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer pesoGramas;

    @Column(nullable = false)
    private Integer estoque;

    // salvo pelo id (TipoProteinaConverter), não pelo nome/ordinal
    @Column(nullable = false)
    private TipoProteina tipoProteina;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_marca", nullable = false)
    private Marca marca;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_fornecedor", nullable = false)
    private Fornecedor fornecedor;

    @ManyToMany
    @JoinTable(name = "produto_sabor",
            joinColumns = @JoinColumn(name = "id_produto"),
            inverseJoinColumns = @JoinColumn(name = "id_sabor"))
    @BatchSize(size = 25)
    private Set<Sabor> sabores = new HashSet<>();

    @Column(length = 255)
    private String imagemKey;

    @Column(length = 160)
    private String imagemNome;

    @Column(length = 80)
    private String imagemContentType;

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

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getPesoGramas() {
        return pesoGramas;
    }

    public void setPesoGramas(Integer pesoGramas) {
        this.pesoGramas = pesoGramas;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public TipoProteina getTipoProteina() {
        return tipoProteina;
    }

    public void setTipoProteina(TipoProteina tipoProteina) {
        this.tipoProteina = tipoProteina;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Set<Sabor> getSabores() {
        return sabores;
    }

    public void setSabores(Set<Sabor> sabores) {
        this.sabores.clear();
        if (sabores != null) {
            this.sabores.addAll(sabores);
        }
    }

    public String getImagemKey() {
        return imagemKey;
    }

    public void setImagemKey(String imagemKey) {
        this.imagemKey = imagemKey;
    }

    public String getImagemNome() {
        return imagemNome;
    }

    public void setImagemNome(String imagemNome) {
        this.imagemNome = imagemNome;
    }

    public String getImagemContentType() {
        return imagemContentType;
    }

    public void setImagemContentType(String imagemContentType) {
        this.imagemContentType = imagemContentType;
    }
}
