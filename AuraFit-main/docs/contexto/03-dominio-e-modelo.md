# 03 — Domínio e Modelo de Dados

Este documento descreve o vocabulário do domínio, as entidades e seus relacionamentos, os diagramas de classes e ER extraídos diretamente do código-fonte e do script de carga inicial, as máquinas de estado e as divergências encontradas entre código, documentação (`diagrama-classes.puml`) e banco de dados (`import.sql`).

---

## 1. Vocabulário do Domínio (Glossário)

| Termo no Código | Onde Vive | Significado Real no Sistema |
|---|---|---|
| **DefaultEntity** | [`DefaultEntity.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/DefaultEntity.java#L12-L56) | Superclasse JPA (`@MappedSuperclass`) compartilhada por todas as 11 entidades persistentes. Fornece `id` (`IDENTITY`), `dataCadastro` (preenchida via `@PrePersist`) e `dataAlteracao` (preenchida via `@PreUpdate`). |
| **Produto** | [`Produto.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Produto.java#L14-L147) | Item comercializado na loja de suplementos (ex.: Whey Protein, Barra de Proteína). Possui preço (`BigDecimal`), peso em gramas, estoque (`Integer`), classificação obrigatória de `TipoProteina`, flag `ativo`, vínculos obrigatórios com `Categoria`, `Marca` e `Fornecedor`, e conjunto opcional (`0..*`) de `Sabor`. |
| **TipoProteina** | [`TipoProteina.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/TipoProteina.java#L7-L42) | Enumeração de domínio que classifica a proteína do suplemento: `CONCENTRADO` (1), `ISOLADO` (2), `HIDROLISADO` (3), `BLEND` (4), `VEGETAL` (5). Persistida no banco pelo `id` numérico (`BIGINT`) via [`TipoProteinaConverter`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/converterjpa/TipoProteinaConverter.java#L7-L19) e serializada em JSON como objeto `{ id, nome }` (`@JsonFormat(shape = OBJECT)`). |
| **Categoria** | [`Categoria.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Categoria.java#L6-L30) | Agrupamento mercadológico de produtos (ex.: "Whey Protein", "Creatina", "Pré-Treino"). Possui `nome` único e `descricao` opcional. |
| **Marca** | [`Marca.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Marca.java#L6-L53) | Fabricante/detentora da marca do suplemento (ex.: "Growth Supplements", "Max Titanium"). Possui `nome`, `cnpj` único (14 dígitos sem máscara), `emailContato` opcional e flag `ativa` (permite inativar sem excluir). |
| **Fornecedor** | [`Fornecedor.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Fornecedor.java#L6-L54) | Pessoa jurídica distribuidora/atacadista que fornece o produto à loja. Possui `razaoSocial`, `cnpj` único (14 dígitos), `telefone` (10 ou 11 dígitos como string única) e `email` opcional. |
| **Sabor** | [`Sabor.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Sabor.java#L6-L30) | Variação sensorial disponível para um produto (ex.: "Chocolate", "Baunilha", "Natural"). Associado a `Produto` em relação N:N (`produto_sabor`), sem controle de estoque individual por sabor. |
| **Pessoa** | [`Pessoa.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Pessoa.java#L19-L106) | Entidade base de pessoa física contendo `nome`, `email` único, `cpf` único (11 dígitos), `dataNascimento`, `genero` opcional, lista de `Telefone` (`1..*` na validação de entrada) e `Endereco` (`1:1`). Projetada para ser reutilizada por composição (`@OneToOne`) por `Cliente` e futuramente `Funcionario`/`Usuario`. |
| **Cliente** | [`Cliente.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Cliente.java#L10-L47) | Papel de consumidor da loja vinculado a uma `Pessoa` por composição (`@OneToOne`, `id_pessoa` único). Adiciona as flags `aceitaNovidades` e `ativo`. Na API e no Front-end, `Cliente` e `Pessoa` são achatados em um único DTO (`ClienteRequestDTO` / `ClienteResponseDTO`). |
| **Telefone** | [`Telefone.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Telefone.java#L8-L55) | Contato telefônico pertencente exclusivamente a uma `Pessoa` (`@ManyToOne`, `orphanRemoval = true`). Separa `codigoArea` (DDD de 2 dígitos), `numero` (8 ou 9 dígitos) e flag `whatsapp`. Diferente do `telefone` de `Fornecedor`, que é armazenado em coluna única. |
| **Endereco** | [`Endereco.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Endereco.java#L8-L78) | Endereço físico vinculado exclusivamente a uma `Pessoa` (`@OneToOne`, `orphanRemoval = true`) e a um `Municipio` (`@ManyToOne`). Armazena `cep` (8 dígitos), `logradouro`, `numero`, `complemento` opcional e `bairro`. |
| **Municipio** | [`Municipio.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Municipio.java#L8-L45) | Cidade brasileira vinculada a um `Estado` (`@ManyToOne`). Possui `nome` e `codigoIbge` (7 dígitos, único, opcional). Pode ser pré-cadastrado via `import.sql` ou criado sob demanda (`buscarOuCriar`) durante o cadastro/edição de `Cliente`. |
| **Estado** | [`Estado.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Estado.java#L6-L41) | Unidade federativa brasileira (26 estados + DF). Possui `nome`, `sigla` única (2 letras) e `Regiao`. Somente leitura na API. |
| **Regiao** | [`Regiao.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Regiao.java#L7-L42) | Macrorregião geográfica do IBGE: `CENTRO_OESTE` (1), `NORTE` (2), `NORDESTE` (3), `SUDESTE` (4), `SUL` (5). Persistida pelo `id` (`BIGINT`) via [`RegiaoConverter`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/converterjpa/RegiaoConverter.java#L7-L19). |
| **Carrinho / ItemCarrinho** | [`carrinho.service.ts`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/services/carrinho.service.ts#L4-L70) | Conceito exclusivo do protótipo front-end (`localStorage` sob chave `aurafit.carrinho`). Agrupa `Produto`, nome do `sabor` selecionado (`string | null`) e `quantidade`. Não existe no back-end. |
| **Sessao / PedidoResumo** | [`sessao.service.ts`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/services/sessao.service.ts#L3-L40) | Conceito exclusivo do protótipo front-end (em memória via Angular Signals). Simula usuário autenticado (`nome`, `email`) e lista de pedidos realizados na sessão, sem persistência no back-end. |

---

## 2. Entidades, Campos, Restrições e Ciclo de Vida

### 2.1. Campos herdados por todas as entidades (`DefaultEntity`)
- `id`: `Long`, PK auto-incrementada (`GenerationType.IDENTITY`).
- `dataCadastro`: `LocalDateTime`, `nullable = true` no DDL (sem `@Column(nullable = false)`), preenchida automaticamente em `@PrePersist`.
- `dataAlteracao`: `LocalDateTime`, `nullable = true`, preenchida automaticamente em `@PreUpdate`.

### 2.2. Detalhamento por Entidade

| Entidade | Tabela | Campo | Tipo Java / SQL | Obrigatório (JPA / Bean Validation) | Único (JPA / Service) | Ciclo de Vida (Criação / Atualização / Exclusão) |
|---|---|---|---|---|---|---|
| **Categoria** | `categoria` | `nome`<br>`descricao` | `String(100)`<br>`String(255)` | Sim (`nullable=false`, `@NotBlank`, 2..100)<br>Não (máx 255) | Sim (`unique=true` + check case-insensitive no service)<br>Não | **Create/Update:** `trim()` em `nome`; `descricao` em branco vira `null`.<br>**Delete:** bloqueado se existir `Produto` vinculado (`existeComCategoria`). |
| **Marca** | `marca` | `nome`<br>`cnpj`<br>`emailContato`<br>`ativa` | `String(100)`<br>`String(14)`<br>`String(100)`<br>`boolean` | Sim (`nullable=false`, `@NotBlank`, 2..100)<br>Sim (`nullable=false`, `\d{14}`, `@CNPJ`)<br>Não (`@Email`, máx 100)<br>Sim (`nullable=false`, `@NotNull`, default `true`) | Não<br>Sim (`unique=true` + check no service)<br>Não<br>Não | **Create/Update:** `trim()` em `nome`; `emailContato` convertido para `toLowerCase()` ou `null`.<br>**Delete:** bloqueado se existir `Produto` vinculado (`existeComMarca`). Mensagem orienta a inativar a marca. |
| **Fornecedor** | `fornecedor` | `razaoSocial`<br>`cnpj`<br>`telefone`<br>`email` | `String(120)`<br>`String(14)`<br>`String(11)`<br>`String(100)` | Sim (`nullable=false`, `@NotBlank`, 2..120)<br>Sim (`nullable=false`, `\d{14}`, `@CNPJ`)<br>Sim (`nullable=false`, `\d{10,11}`)<br>Não (`@Email`, máx 100) | Não<br>Sim (`unique=true` + check no service)<br>Não<br>Não | **Create/Update:** `trim()` em `razaoSocial`; `email` convertido para `toLowerCase()` ou `null`.<br>**Delete:** bloqueado se existir `Produto` vinculado (`existeComFornecedor`). |
| **Sabor** | `sabor` | `nome`<br>`descricao` | `String(80)`<br>`String(255)` | Sim (`nullable=false`, `@NotBlank`, 2..80)<br>Não (máx 255) | Sim (`unique=true` + check case-insensitive no service)<br>Não | **Create/Update:** `trim()` em `nome`; `descricao` vazia vira `null`.<br>**Delete:** bloqueado se existir `Produto` vinculado em `produto_sabor` (`existeComSabor`). |
| **Produto** | `produto` | `nome`<br>`descricao`<br>`preco`<br>`pesoGramas`<br>`estoque`<br>`tipoProteina`<br>`ativo`<br>`categoria`<br>`marca`<br>`fornecedor`<br>`sabores` | `String(120)`<br>`String(1000)`<br>`BigDecimal(10,2)`<br>`Integer`<br>`Integer`<br>`TipoProteina (BIGINT)`<br>`boolean`<br>`ManyToOne`<br>`ManyToOne`<br>`ManyToOne`<br>`ManyToMany` | Sim (2..120)<br>Não (máx 1000)<br>Sim (`>= 0.01`, `Digits(8,2)`)<br>Sim (`@Positive`)<br>Sim (`@Min(0)`)<br>Sim (`nullable=false`)<br>Sim (default `true`)<br>Sim (`id_categoria`)<br>Sim (`id_marca`)<br>Sim (`id_fornecedor`)<br>Não (`Set<Sabor>`) | Não | **Create/Update:** exige marca ativa se for produto novo ou se estiver trocando de marca; substitui conjunto `sabores` (`clear()` + `addAll()`).<br>**Delete:** exclusão física direta (`produtoRepository.delete`), removendo linhas da tabela associativa `produto_sabor`. |
| **Pessoa** | `pessoa` | `nome`<br>`email`<br>`cpf`<br>`dataNascimento`<br>`genero`<br>`telefones`<br>`endereco` | `String(120)`<br>`String(120)`<br>`String(11)`<br>`LocalDate`<br>`String(30)`<br>`OneToMany`<br>`OneToOne` | Sim (2..120)<br>Sim (`@Email`, máx 120)<br>Sim (`\d{11}`, `@CPF`)<br>Sim (`@Past`, >= 16 anos)<br>Não (máx 30)<br>Sim no DTO (`@NotEmpty`)<br>Sim no DTO (`@NotNull`), opcional no JPA | Não<br>Sim (`unique=true` + check case-insensitive)<br>Sim (`unique=true` + check exato)<br>Não<br>Não<br>Não<br>`id_endereco` `unique=true` | Gerenciada exclusivamente dentro do ciclo de vida de `Cliente`.<br>**Update:** `telefones.clear()` apaga todos os telefones anteriores (`orphanRemoval=true`) e reinsere os novos; `Endereco` existente é reaproveitado (mesmo `id`). |
| **Telefone** | `telefone` | `pessoa`<br>`codigoArea`<br>`numero`<br>`whatsapp` | `ManyToOne`<br>`String(2)`<br>`String(9)`<br>`boolean` | Sim (`id_pessoa`)<br>Sim (`\d{2}`)<br>Sim (`\d{8,9}`)<br>Sim (`@NotNull`) | Não | Criado e destruído em cascata a partir de `Pessoa.setTelefones(...)`. A cada `PUT /clientes/{id}`, todos os registros de `Telefone` daquela `Pessoa` são deletados e recriados com novos IDs. |
| **Endereco** | `endereco` | `cep`<br>`logradouro`<br>`numero`<br>`complemento`<br>`bairro`<br>`municipio` | `String(8)`<br>`String(120)`<br>`String(10)`<br>`String(60)`<br>`String(80)`<br>`ManyToOne` | Sim (`\d{8}`)<br>Sim (máx 120)<br>Sim (máx 10)<br>Não (máx 60)<br>Sim (máx 80)<br>Sim (`id_municipio`) | Não | Criado junto com `Pessoa` no `POST /clientes`, atualizado in-place no `PUT /clientes/{id}` e removido em cascata (`CascadeType.ALL, orphanRemoval = true`) quando `Pessoa` é deletada. |
| **Cliente** | `cliente` | `pessoa`<br>`aceitaNovidades`<br>`ativo` | `OneToOne(LAZY)`<br>`boolean`<br>`boolean` | Sim (`id_pessoa`, `nullable=false`)<br>Sim<br>Sim (default `true`) | `id_pessoa` `unique=true` | **Create:** instancia `Cliente` e `Pessoa` juntos (`CascadeType.PERSIST, MERGE`).<br>**Delete:** executa `clienteRepository.delete(cliente)` seguido explicitamente de `pessoaRepository.delete(pessoa)` (pois `Cliente.pessoa` não possui `CascadeType.REMOVE`). |
| **Municipio** | `municipio` | `nome`<br>`codigoIbge`<br>`estado` | `String(80)`<br>`String(7)`<br>`ManyToOne` | Sim (máx 80)<br>Não (`\d{7}` se informado)<br>Sim (`id_estado`) | Não<br>Sim (`unique=true`)<br>Não | **Leitura/Auto-cadastro:** não possui endpoints REST de escrita, mas `MunicipioServiceImpl.buscarOuCriar` insere novo `Municipio` ou preenche `codigoIbge` faltante em município existente durante `create`/`update` de `Cliente`. |
| **Estado** | `estado` | `nome`<br>`sigla`<br>`regiao` | `String(60)`<br>`String(2)`<br>`Regiao (BIGINT)` | Sim<br>Sim<br>Sim | Não<br>Sim (`unique=true`)<br>Não | **Somente leitura:** populado exclusivamente via `import.sql` (27 UFs). |

---

## 3. Diagrama de Classes de Domínio (Mermaid)

Reflete fielmente as classes em `br.unitins.topicos1.aurafit.model`:

```mermaid
classDiagram
    class DefaultEntity {
        <<MappedSuperclass>>
        -Long id
        -LocalDateTime dataCadastro
        -LocalDateTime dataAlteracao
        +registrarDataCadastro() void
        +registrarDataAlteracao() void
    }

    class Categoria {
        -String nome
        -String descricao
    }

    class Marca {
        -String nome
        -String cnpj
        -String emailContato
        -boolean ativa
    }

    class Fornecedor {
        -String razaoSocial
        -String cnpj
        -String telefone
        -String email
    }

    class Sabor {
        -String nome
        -String descricao
    }

    class Produto {
        -String nome
        -String descricao
        -BigDecimal preco
        -Integer pesoGramas
        -Integer estoque
        -TipoProteina tipoProteina
        -boolean ativo
        -Categoria categoria
        -Marca marca
        -Fornecedor fornecedor
        -Set~Sabor~ sabores
    }

    class TipoProteina {
        <<enumeration>>
        CONCENTRADO(1, "Concentrado")
        ISOLADO(2, "Isolado")
        HIDROLISADO(3, "Hidrolisado")
        BLEND(4, "Blend")
        VEGETAL(5, "Vegetal")
        -Long id
        -String nome
        +valueOf(Long id)$ TipoProteina
    }

    class Cliente {
        -Pessoa pessoa
        -boolean aceitaNovidades
        -boolean ativo
    }

    class Pessoa {
        -String nome
        -String email
        -String cpf
        -LocalDate dataNascimento
        -String genero
        -List~Telefone~ telefones
        -Endereco endereco
    }

    class Telefone {
        -Pessoa pessoa
        -String codigoArea
        -String numero
        -boolean whatsapp
    }

    class Endereco {
        -String cep
        -String logradouro
        -String numero
        -String complemento
        -String bairro
        -Municipio municipio
    }

    class Municipio {
        -String nome
        -String codigoIbge
        -Estado estado
    }

    class Estado {
        -String nome
        -String sigla
        -Regiao regiao
    }

    class Regiao {
        <<enumeration>>
        CENTRO_OESTE(1, "Centro-Oeste")
        NORTE(2, "Norte")
        NORDESTE(3, "Nordeste")
        SUDESTE(4, "Sudeste")
        SUL(5, "Sul")
        -Long id
        -String nome
        +valueOf(Long id)$ Regiao
    }

    DefaultEntity <|-- Categoria
    DefaultEntity <|-- Marca
    DefaultEntity <|-- Fornecedor
    DefaultEntity <|-- Sabor
    DefaultEntity <|-- Produto
    DefaultEntity <|-- Cliente
    DefaultEntity <|-- Pessoa
    DefaultEntity <|-- Telefone
    DefaultEntity <|-- Endereco
    DefaultEntity <|-- Municipio
    DefaultEntity <|-- Estado

    Produto "*" --> "1" Categoria : id_categoria
    Produto "*" --> "1" Marca : id_marca
    Produto "*" --> "1" Fornecedor : id_fornecedor
    Produto "*" --> "*" Sabor : produto_sabor
    Produto --> "1" TipoProteina : TipoProteinaConverter

    Cliente "1" --> "1" Pessoa : id_pessoa (LAZY, PERSIST/MERGE)
    Pessoa "1" *-- "*" Telefone : mappedBy="pessoa" (ALL, orphanRemoval)
    Pessoa "1" *-- "0..1" Endereco : id_endereco (ALL, orphanRemoval)
    Endereco "*" --> "1" Municipio : id_municipio
    Municipio "*" --> "1" Estado : id_estado
    Estado --> "1" Regiao : RegiaoConverter
```

---

## 4. Diagrama Entidade-Relacionamento — ER (Mermaid)

Reflete o schema físico gerado pelo Hibernate ORM (`drop-and-create`) no PostgreSQL e alimentado por [`import.sql`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/import.sql):

```mermaid
erDiagram
    ESTADO ||--o{ MUNICIPIO : "id_estado"
    MUNICIPIO ||--o{ ENDERECO : "id_municipio"
    ENDERECO |o--|| PESSOA : "id_endereco (UK)"
    PESSOA ||--o{ TELEFONE : "id_pessoa"
    PESSOA ||--o| CLIENTE : "id_pessoa (UK)"

    CATEGORIA ||--o{ PRODUTO : "id_categoria"
    MARCA ||--o{ PRODUTO : "id_marca"
    FORNECEDOR ||--o{ PRODUTO : "id_fornecedor"
    PRODUTO }o--o{ SABOR : "produto_sabor"

    ESTADO {
        bigint id PK
        varchar(60) nome "NOT NULL"
        varchar(2) sigla "NOT NULL, UNIQUE"
        bigint regiao "NOT NULL (1..5)"
        timestamp dataCadastro
        timestamp dataAlteracao
    }

    MUNICIPIO {
        bigint id PK
        varchar(80) nome "NOT NULL"
        varchar(7) codigoIbge "UNIQUE, NULLABLE"
        bigint id_estado FK "NOT NULL"
        timestamp dataCadastro
        timestamp dataAlteracao
    }

    ENDERECO {
        bigint id PK
        varchar(8) cep "NOT NULL"
        varchar(120) logradouro "NOT NULL"
        varchar(10) numero "NOT NULL"
        varchar(60) complemento "NULLABLE"
        varchar(80) bairro "NOT NULL"
        bigint id_municipio FK "NOT NULL"
        timestamp dataCadastro
        timestamp dataAlteracao
    }

    PESSOA {
        bigint id PK
        varchar(120) nome "NOT NULL"
        varchar(120) email "NOT NULL, UNIQUE"
        varchar(11) cpf "NOT NULL, UNIQUE"
        date dataNascimento "NOT NULL"
        varchar(30) genero "NULLABLE"
        bigint id_endereco FK "UNIQUE, NULLABLE"
        timestamp dataCadastro
        timestamp dataAlteracao
    }

    TELEFONE {
        bigint id PK
        bigint id_pessoa FK "NOT NULL"
        varchar(2) codigoArea "NOT NULL"
        varchar(9) numero "NOT NULL"
        boolean whatsapp "NOT NULL"
        timestamp dataCadastro
        timestamp dataAlteracao
    }

    CLIENTE {
        bigint id PK
        bigint id_pessoa FK "NOT NULL, UNIQUE"
        boolean aceitaNovidades "NOT NULL"
        boolean ativo "NOT NULL"
        timestamp dataCadastro
        timestamp dataAlteracao
    }

    CATEGORIA {
        bigint id PK
        varchar(100) nome "NOT NULL, UNIQUE"
        varchar(255) descricao "NULLABLE"
        timestamp dataCadastro
        timestamp dataAlteracao
    }

    MARCA {
        bigint id PK
        varchar(100) nome "NOT NULL"
        varchar(14) cnpj "NOT NULL, UNIQUE"
        varchar(100) emailContato "NULLABLE"
        boolean ativa "NOT NULL"
        timestamp dataCadastro
        timestamp dataAlteracao
    }

    FORNECEDOR {
        bigint id PK
        varchar(120) razaoSocial "NOT NULL"
        varchar(14) cnpj "NOT NULL, UNIQUE"
        varchar(11) telefone "NOT NULL"
        varchar(100) email "NULLABLE"
        timestamp dataCadastro
        timestamp dataAlteracao
    }

    SABOR {
        bigint id PK
        varchar(80) nome "NOT NULL, UNIQUE"
        varchar(255) descricao "NULLABLE"
        timestamp dataCadastro
        timestamp dataAlteracao
    }

    PRODUTO {
        bigint id PK
        varchar(120) nome "NOT NULL"
        varchar(1000) descricao "NULLABLE"
        numeric(10_2) preco "NOT NULL"
        integer pesoGramas "NOT NULL"
        integer estoque "NOT NULL"
        bigint tipoProteina "NOT NULL (1..5)"
        boolean ativo "NOT NULL"
        bigint id_categoria FK "NOT NULL"
        bigint id_marca FK "NOT NULL"
        bigint id_fornecedor FK "NOT NULL"
        timestamp dataCadastro
        timestamp dataAlteracao
    }

    PRODUTO_SABOR {
        bigint id_produto PK, FK
        bigint id_sabor PK, FK
    }
```

---

## 5. Divergências entre Código, Documentação (`diagrama-classes.puml`), DTOs e Banco (`import.sql`)

1. **Obrigatoriedade de `Endereco` em `Pessoa` (JPA vs. DTO vs. Diagrama):**
   - Em [`Pessoa.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Pessoa.java#L40-L42) e em [`diagrama-classes.puml`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/docs/diagrama-classes.puml#L119), o relacionamento `Pessoa -> Endereco` é opcional (`0..1`, `@OneToOne` sem `optional = false` e `@JoinColumn(name = "id_endereco", unique = true)` sem `nullable = false`).
   - Entretanto, em [`ClienteRequestDTO.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/dto/ClienteRequestDTO.java#L42-L44) o campo `endereco` é anotado com `@NotNull`, e [`ClienteServiceImpl.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/ClienteServiceImpl.java#L129-L144) assume que `dto.endereco()` nunca é nulo (acessando `dto.endereco().cep()` diretamente). Logo, na prática, nenhuma pessoa pode ser cadastrada via API sem endereço (`1..1`).

2. **Unicidade de `(nome, id_estado)` em `Municipio` (Código vs. Constraint de Banco):**
   - [`MunicipioServiceImpl.buscarOuCriar`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/MunicipioServiceImpl.java#L58-L60) trata a combinação `(lower(nome), estado.id)` como chave alternativa quando `codigoIbge` não é informado ou não é encontrado.
   - Contudo, [`Municipio.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Municipio.java#L8-L20) define `unique = true` apenas em `codigoIbge` (que permite múltiplos `NULL` no PostgreSQL), sem `@Table(uniqueConstraints = ...)` para `(nome, id_estado)`.

3. **Unicidade Case-Insensitive no Código vs. Case-Sensitive no Banco:**
   - Para `Categoria.nome`, `Sabor.nome` e `Pessoa.email`, os repositórios validam unicidade usando `lower(campo) = lower(?1)`.
   - Já a constraint `@Column(unique = true)` gerada no PostgreSQL é case-sensitive (collation padrão). Além disso, enquanto `Pessoa.email`, `Marca.emailContato` e `Fornecedor.email` são convertidos para `toLowerCase()` antes de salvar, `Categoria.nome` e `Sabor.nome` preservam maiúsculas/minúsculas originais.

4. **Representação de Telefone em `Fornecedor` vs. `Pessoa`:**
   - Em `Pessoa`, telefones são modelados como entidade própria [`Telefone`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Telefone.java) com `codigoArea` (2 dígitos), `numero` (8 ou 9 dígitos) e `whatsapp` (`boolean`).
   - Em [`Fornecedor`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Fornecedor.java#L17-L18), `telefone` é uma `String(11)` única contendo DDD + número concatenados.

5. **Exposição de `dataCadastro` e `dataAlteracao` nos ResponseDTOs:**
   - Todas as 11 entidades herdam `dataCadastro` e `dataAlteracao` de `DefaultEntity`.
   - Porém, apenas [`ClienteResponseDTO`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/dto/ClienteResponseDTO.java#L18) expõe `dataCadastro` (obtido de `cliente.getDataCadastro()`). Nenhuma outra entidade expõe `dataCadastro`, e **nenhum** DTO do sistema expõe `dataAlteracao`.

6. **Exclusão de `Pessoa` ao excluir `Cliente` vs. Comentário de Reuso Futuro:**
   - O Javadoc de [`Pessoa.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Pessoa.java#L14-L18) e a nota em [`diagrama-classes.puml`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/docs/diagrama-classes.puml#L124) afirmam que `Pessoa` será reutilizada por composição por `Usuario` e `Funcionario`.
   - Porém, [`ClienteServiceImpl.delete`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/ClienteServiceImpl.java#L92-L98) executa `pessoaRepository.delete(pessoa)` incondicionalmente sob a premissa comentada na linha 96: `// a pessoa (com telefones e endereço) pertence apenas ao cliente`. Quando `Usuario`/`Funcionario` compartilharem a mesma `Pessoa`, essa exclusão falhará por violação de FK ou apagará a `Pessoa` de outro papel.

---

## 6. Máquinas de Estado das Entidades Principais

O sistema não possui entidades com máquinas de estado complexas (como pedidos com fluxo de status no back-end). Os estados existentes no back-end são booleanos (`ativa`/`ativo`), e no front-end há um estado simulado de pedido.

### 6.1. Estado de `Marca` (`ativa: boolean`)
```mermaid
stateDiagram-v2
    [*] --> Ativa : POST /marcas (ativa = true)
    [*] --> Inativa : POST /marcas (ativa = false)
    Ativa --> Inativa : PUT /marcas/{id} (ativa = false)
    Inativa --> Ativa : PUT /marcas/{id} (ativa = true)
    Ativa --> [*] : DELETE /marcas/{id} (somente se sem produtos)
    Inativa --> [*] : DELETE /marcas/{id} (somente se sem produtos)
```
- **Regras de transição e efeitos:**
  - Uma `Marca` pode ser inativada (`ativa = false`) mesmo possuindo produtos ativos vinculados a ela.
  - Quando `Marca` está `Inativa`, **novos produtos** não podem ser criados com ela e **produtos de outras marcas** não podem ser transferidos para ela ([`ProdutoServiceImpl.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/ProdutoServiceImpl.java#L112-L115)).
  - **Transição permitida (atenção):** um `Produto` que **já** pertencia à `Marca` antes de ela ser inativada pode continuar sendo editado normalmente (inclusive mantendo `produto.ativo = true`), pois a trava verifica `trocouDeMarca && !marca.isAtiva()`.

### 6.2. Estado de `Produto` (`ativo: boolean` e `estoque: Integer`)
```mermaid
stateDiagram-v2
    [*] --> AtivoComEstoque : POST /produtos (ativo=true, estoque>0)
    [*] --> AtivoSemEstoque : POST /produtos (ativo=true, estoque=0)
    [*] --> Inativo : POST /produtos (ativo=false)
    AtivoComEstoque --> AtivoSemEstoque : PUT /produtos/{id} (estoque=0)
    AtivoSemEstoque --> AtivoComEstoque : PUT /produtos/{id} (estoque>0)
    AtivoComEstoque --> Inativo : PUT /produtos/{id} (ativo=false)
    AtivoSemEstoque --> Inativo : PUT /produtos/{id} (ativo=false)
    Inativo --> AtivoComEstoque : PUT /produtos/{id} (ativo=true, estoque>0)
    Inativo --> AtivoSemEstoque : PUT /produtos/{id} (ativo=true, estoque=0)
    AtivoComEstoque --> [*] : DELETE /produtos/{id}
    AtivoSemEstoque --> [*] : DELETE /produtos/{id}
    Inativo --> [*] : DELETE /produtos/{id}
```
- **Transições possíveis mas potencialmente indevidas:**
  - Não há vínculo automático entre `estoque == 0` e `ativo == false` no back-end: um produto pode estar `ativo = true` com `estoque = 0` (no `import.sql`, o produto 7 tem `estoque = 0` e `ativo = false`, mas a API aceita `ativo = true` e `estoque = 0`).
  - No catálogo da loja ([`catalogo.ts`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/catalogo/catalogo.ts)), o endpoint `GET /produtos` do back-end **não filtra** produtos inativos (`ativo = false`); o filtro ou exibição depende do front-end.
  - Qualquer `Produto` pode ser excluído fisicamente (`DELETE /produtos/{id}`) sem restrições, pois ainda não existe entidade `Pedido`/`ItemPedido` no back-end.
