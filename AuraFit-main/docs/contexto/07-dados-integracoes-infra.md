# 07 — Dados, Integrações e Infraestrutura

Este documento analisa o schema do banco de dados, estratégia de migração/carga, índices, restrições, problemas de performance em consultas (como N+1), limites transacionais, integrações externas (ViaCEP e Google Fonts) e configuração de infraestrutura/ambientes.

---

## 1. Banco de Dados, Schema e Migrações

### 1.1. Estratégia de Geração de Schema e Carga (`drop-and-create`)
- **Comportamento Atual Confirmado:**
  - Não há ferramenta de versionamento de migrações (Flyway ou Liquibase).
  - Em [`application.properties`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/application.properties#L7), está configurado `quarkus.hibernate-orm.database.generation=drop-and-create` sem prefixo de perfil (`%dev`), o que significa que **em qualquer perfil de execução (inclusive produção, caso não seja sobrescrito por variável de ambiente)**, o Hibernate apaga (`DROP TABLE`) todas as tabelas ao iniciar a aplicação, recria o DDL a partir das entidades JPA e executa [`import.sql`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/import.sql).
- **Consistência entre `import.sql` e as Entidades JPA:**
  - `import.sql` insere:
    - 27 registros em `estado` (com `regiao` = `1..5` correspondente a `RegiaoConverter`).
    - 10 capitais/cidades em `municipio` (localizando `id_estado` por subconsulta `select id from estado where sigla = ...`).
    - 5 registros em `categoria`, 5 em `marca` (incluindo 1 marca inativa: ID 5), 3 em `fornecedor`, 5 em `sabor`.
    - 8 registros em `produto` (incluindo 1 produto inativo e sem estoque: ID 7) e 13 vínculos em `produto_sabor`.
    - 3 registros em `endereco`, 3 em `pessoa`, 4 em `telefone` e 3 em `cliente`.
  - Todos os CPFs e CNPJs inseridos em `import.sql` possuem dígitos verificadores válidos segundo as regras do Hibernate Validator (`@CPF` / `@CNPJ`), mantendo 100% de consistência com as regras de negócio da aplicação.

### 1.2. Índices e Restrições (Constraints) no Banco
- **Chaves Primárias e Unique Constraints geradas pelo JPA:**
  - `categoria(nome)` — UNIQUE
  - `sabor(nome)` — UNIQUE
  - `marca(cnpj)` — UNIQUE
  - `fornecedor(cnpj)` — UNIQUE
  - `pessoa(email)`, `pessoa(cpf)`, `pessoa(id_endereco)` — UNIQUE
  - `cliente(id_pessoa)` — UNIQUE
  - `municipio(codigoIbge)` — UNIQUE
  - `estado(sigla)` — UNIQUE
  - `produto_sabor(id_produto, id_sabor)` — PK composta (porque `Produto.sabores` é `Set<Sabor>`).
- **Índices Ausentes:**
  - Não há anotações `@Table(indexes = ...)` em nenhuma entidade.
  - No PostgreSQL, colunas de chave estrangeira (`produto.id_categoria`, `produto.id_marca`, `produto.id_fornecedor`, `municipio.id_estado`, `endereco.id_municipio`, `telefone.id_pessoa`) **não recebem índice automaticamente** (apenas PKs e UNIQUEs criam índices B-Tree). Consultas frequentes como `ProdutoRepository.buscar` (filtrando por `id_categoria`, `id_marca`, `tipoProteina`) e as verificações de exclusão (`existeComCategoria`, `existeComMarca`, `existeComFornecedor`) fazem *Sequential Scan* na tabela `produto`.
  - Buscas textuais com `lower(nome) like lower('%termo%')` em todos os repositórios não utilizam índice B-Tree padrão.

---

## 2. Análise de Consultas e Riscos de N+1

### 2.1. Problema de N+1 Confirmado na Listagem de Produtos (`GET /produtos`)
- Em [`Produto.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Produto.java#L39-L55):
  - `categoria`, `marca` e `fornecedor` são `@ManyToOne` (cujo default no JPA é `FetchType.EAGER`).
  - `sabores` é `@ManyToMany` (cujo default no JPA é `FetchType.LAZY`).
- Em [`ProdutoRepository.buscar`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/repository/ProdutoRepository.java#L22-L45), a consulta HQL não faz `LEFT JOIN FETCH`.
- Quando `ProdutoServiceImpl.paginar` (L103-L108) executa `query.page(page, pageSize).list()` e mapeia cada produto com `ProdutoMapper::toResponse`:
  1. Executa 1 `SELECT` para buscar a página de `Produto` + 1 `SELECT count(*)` para o total.
  2. Para **cada** produto da página, o Hibernate dispara `SELECT` adicional em `produto_sabor JOIN sabor` ao acessar `produto.getSabores()` em `ProdutoMapper.java:27` (e `SELECT` para `Categoria`, `Marca` e `Fornecedor` caso não estejam no cache de 1º nível da sessão).

### 2.2. Problema de N+1 Confirmado na Listagem de Clientes (`GET /clientes`)
- Em [`Cliente.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Cliente.java#L13): `pessoa` é `@OneToOne(fetch = FetchType.LAZY)`.
- Em [`Pessoa.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Pessoa.java#L37-L42): `telefones` é `@OneToMany` (`LAZY`) e `endereco` é `@OneToOne` (`EAGER`, com `municipio` `EAGER` e `estado` `EAGER`).
- Em [`ClienteRepository.findAll`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/repository/ClienteRepository.java#L20-L22), a query `find("order by pessoa.nome")` busca apenas `Cliente`. Ao converter a lista em `ClienteMapper.toResponse`, para **cada** cliente da página são disparadas queries adicionais para carregar `Pessoa` (com `Endereco`/`Municipio`/`Estado`) e otra query para carregar `pessoa.getTelefones()`.

### 2.3. Consultas em Loop ao Salvar Produto (`POST /produtos` e `PUT /produtos/{id}`)
- Em [`ProdutoServiceImpl.buscarSabores`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/ProdutoServiceImpl.java#L162-L175), cada ID de `idsSabores` é buscado individualmente no banco dentro de um `for`: `saborRepository.findById(id)`.

---

## 3. Delimitação de Transações

- **Leituras com `@Transactional`:** `ProdutoServiceImpl` e `ClienteServiceImpl` marcam todos os métodos de leitura (`findAll`, `findByNome`, `buscar`, `findById`, `findByCpf`) com `@Transactional` para evitar `LazyInitializationException` no `ProdutoMapper` e `ClienteMapper`.
- **Leituras sem `@Transactional`:** `CategoriaServiceImpl`, `MarcaServiceImpl`, `FornecedorServiceImpl`, `SaborServiceImpl`, `EstadoServiceImpl` e `MunicipioServiceImpl` não usam `@Transactional` nos métodos `find*`, funcionando corretamente porque suas entidades não possuem coleções ou associações `LAZY`.
- **Escritas:** Todos os métodos `create`, `update`, `delete` e `MunicipioServiceImpl.buscarOuCriar` possuem `@Transactional`.

---

## 4. Integrações Externas

### 4.1. Integração com ViaCEP (`https://viacep.com.br/ws/{cep}/json/`)
- **Onde vive:** Exclusivamente no front-end Angular ([`cep.service.ts`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/services/cep.service.ts#L12-L16)), consumida por [`ClienteForm`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/admin/clientes/cliente-form/cliente-form.ts#L157) e [`Pagamento`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/pagamento/pagamento.ts#L76).
- **Contrato esperado (`EnderecoViaCep`):** `{ cep, logradouro, complemento, bairro, localidade, uf, ibge, erro?: boolean | string }`.
- **Tratamento de falhas:**
  - Em `ClienteForm`: trata tanto CEP inexistente (`resposta.erro` → `null`, exibindo aviso e liberando preenchimento manual de cidade/UF) quanto falha de rede (`error` no `subscribe`, ativando signal `falhaRedeCep` e liberando cidade/UF).
- **Timeouts e Retries:** **Ausentes.** `CepService.consultar` usa `this.http.get(...)` sem operador `timeout(...)` nem `retry(...)` do RxJS. Se o ViaCEP demorar a responder, o spinner `buscandoCep` ficará travado até o timeout padrão do navegador.

### 4.2. Integração com Google Fonts (`fonts.googleapis.com`)
- Referenciada em [`aurafit-frontend/src/index.html`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/index.html#L10-L14).
- **Impacto no Build:** Durante `ng build` (configuração `production`), o builder do Angular tenta baixar o CSS e os arquivos de fonte de `fonts.googleapis.com` para fazer inlining no HTML. Em ambientes de build sem acesso à internet (como CI isolado ou sandbox), o build de produção falha com `getaddrinfo ENOTFOUND fonts.googleapis.com`.

---

## 5. Configuração por Ambiente, Segredos e Infraestrutura

### 5.1. Gestão de Configuração e Segredos
- **Back-end ([`application.properties`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/application.properties)):**
  - Não existe arquivo `.env.example` nem separação de propriedades por perfil (`%dev.`, `%test.`, `%prod.`), exceto uma linha comentada `# %prod.problem.base-url=https://api.aurafit.com.br` na linha 26.
  - **Segredo em texto claro (Achado de Segurança):** A senha do banco de dados PostgreSQL está gravada em texto claro em [`application.properties`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/application.properties#L4) e documentada em [`aurafit-main/README.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/README.md#L11).
  - Logs SQL com parâmetros (`quarkus.hibernate-orm.log.sql=true` e `quarkus.hibernate-orm.log.bind-parameters=true` nas linhas 12–13) estão habilitados globalmente para todos os perfis, expondo dados pessoais (CPF, e-mail, endereço) nos logs caso executados fora de dev.
- **Front-end ([`api.config.ts`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/api.config.ts#L2)):**
  - A URL da API (`http://localhost:8080`) é uma constante hardcoded em `src/app/api.config.ts`, sem sistema de `environments/environment.prod.ts` no `angular.json`.

### 5.2. Docker, Build e CI
- **Docker:** Existem os 4 Dockerfiles gerados pelo scaffold do Quarkus em `aurafit-main/src/main/docker/`. Não há `Dockerfile` para o `aurafit-frontend`, nem `docker-compose.yml` para subir o PostgreSQL (`topicos2db`) junto com a API e o front-end.
- **Maven Wrapper:** `aurafit-main/.mvn/wrapper/maven-wrapper.properties` existe, mas `.mvn/wrapper/maven-wrapper.jar` não está presente, fazendo `mvnw.cmd` depender de acesso direto à internet via PowerShell `DownloadFile` para baixar o Maven na primeira execução.
