# 08 — Qualidade, Segurança e Testes

Este documento avalia tratamento de erros, validação de entradas, segurança (autenticação, autorização, exposição de dados e segredos), concorrência/consistência de dados, cobertura real de testes cruzada com o Catálogo de Regras de Negócio e qualidade de código.

---

## 1. Tratamento de Erros e Validação

### 1.1. Pontos Fortes
- **Padronização RFC 7807 (`application/problem+json`):** Todos os erros no back-end passam por [`ProblemFactory`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/exception/ProblemFactory.java) e pelos 5 ExceptionMappers (`ConstraintViolationExceptionMapper`, `ValidationExceptionMapper`, `ResourceNotFoundExceptionMapper`, `NotFoundExceptionMapper`, `UncaughtExceptionMapper`).
- **Não vazamento de stack traces em HTTP 500:** [`UncaughtExceptionMapper.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/exception/UncaughtExceptionMapper.java#L37-L41) registra a exceção completa no log do servidor (`LOG.errorf`) e devolve ao cliente HTTP apenas a mensagem genérica *"Ocorreu um erro inesperado. Tente novamente mais tarde."*.
- **Integração de erros por campo com o Front-end:** `ConstraintViolationExceptionMapper.caminhoDoCampo` (L48-L63) transforma o `PropertyPath` do Bean Validation (ex.: `create.arg0.telefones[0].numero`) em `"telefones.0.numero"`, que casa diretamente com `form.get(erroCampo.field)` em [`erros-formulario.ts`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/shared/erros-formulario.ts#L49).

### 1.2. Fragilidades e Entradas Não Validadas ou Inconsistentes
1. **Ordem entre `@Length(min = 2)` e `.trim()`:**
   - O Bean Validation valida `@Length(min = 2)` sobre a string bruta recebida no DTO **antes** de o Service aplicar `.trim()`.
   - Se o cliente HTTP (ou `CategoriaForm`/`SaborForm`, que não fazem `.trim()` no front-end) enviar `" a "` (espaço, letra, espaço), `@NotBlank` e `@Length(min = 2)` passam, e o Service salva `"a"` (1 caractere) no banco.
2. **Violação de Constraint de Banco em Concorrência retorna HTTP 500 Genérico:**
   - Não existe `ExceptionMapper` específico para `PersistenceException` / `ConstraintViolationException` do Hibernate ORM (ex.: violação de UNIQUE no PostgreSQL). Se uma constraint de banco falhar, cai em `UncaughtExceptionMapper` retornando HTTP 500 em vez de 400/409.
3. **JSON Malformado ou Tipo Inválido (ex.: letras em campo numérico ou data inválida):**
   - Cai em `UncaughtExceptionMapper` (L29-L35) como `WebApplicationException` `< 500`, retornando HTTP 400 com detalhe genérico (`"A requisição não pôde ser processada. Verifique os dados enviados."`) sem indicar qual campo falhou na desserialização.

---

## 2. Segurança

### 2.1. Autenticação e Autorização
- **Ausência total de autenticação e autorização (Confirmado):**
  - Conforme documentado nos próprios `README.md` (a autenticação Usuário/JWT está prevista para a próxima etapa da disciplina), **todos os 42 endpoints da API Quarkus são 100% abertos**.
  - Qualquer requisição anônima pode listar todos os clientes com seus CPFs, e-mails, telefones, datas de nascimento e endereços (`GET /clientes`), bem como criar, alterar ou apagar produtos, marcas, categorias e clientes.
  - No front-end, as rotas `/admin/*` não possuem Route Guards (`canActivate`).

### 2.2. Credenciais e Segredos Expostos
- **Existência e localização de credencial hardcoded:**
  - Arquivo [`aurafit-main/src/main/resources/application.properties`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/application.properties#L3-L4) (linhas 3 e 4: usuário e senha do banco PostgreSQL em texto claro).
  - Arquivo [`aurafit-main/README.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/README.md#L11) (linha 11: mesmas credenciais documentadas).

### 2.3. Injeção de SQL / HQL e XSS
- **SQL/HQL Injection:** **Seguro.** Todas as consultas nos 9 repositórios utilizam parâmetros posicionais (`?1`, `?2`) ou nomeados (`:nome`, `:idCategoria`) via Panache. Nenhuma string de usuário é concatenada na cláusula HQL.
- **XSS no Front-end:** **Seguro.** O Angular escapa automaticamente interpolações `{{ ... }}` em todos os templates HTML, e não há uso de `bypassSecurityTrustHtml` ou `innerHTML`.

### 2.4. Configurações Inseguras para Produção
- `quarkus.hibernate-orm.database.generation=drop-and-create` ativo sem restrição ao perfil `%dev` ([`application.properties:7`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/application.properties#L7)).
- `quarkus.hibernate-orm.log.bind-parameters=true` ativo globalmente ([`application.properties:13`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/application.properties#L13)), gravando dados pessoais (PII) em logs.

---

## 3. Concorrência e Consistência de Dados

1. **Condição de Corrida (TOCTOU) nas Validações de Unicidade e Auto-Cadastro de Município:**
   - Todos os Services verificam unicidade fazendo `SELECT` antes do `INSERT`/`UPDATE` (`findByNomeExceptId`, `findByCnpjExceptId`, `findByCpfExceptId`, `findByEmailExceptId`).
   - Da mesma forma, [`MunicipioServiceImpl.buscarOuCriar`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/MunicipioServiceImpl.java#L54-L77) consulta se o município existe e, se não existir, executa `persist`.
   - Em requisições concorrentes simultâneas:
     - Para `Categoria`, `Sabor`, `Marca`, `Fornecedor`, `Pessoa` e `Municipio.codigoIbge`, a constraint `UNIQUE` do banco impede dados duplicados, mas lança exceção de banco que vira HTTP 500.
     - Para `Municipio` cadastrado **sem** `codigoIbge` (busca por `nome` + `estado.id`), **não há constraint UNIQUE composta `(nome, id_estado)` no banco**, permitindo a criação de municípios duplicados.
2. **Condição de Corrida na Exclusão vs. Vinculação de Produto:**
   - `CategoriaServiceImpl.delete` verifica `produtoRepository.existeComCategoria(id)` antes de deletar. Se outra transação concorrente inserir um `Produto` com essa categoria simultaneamente, a constraint de FK do PostgreSQL bloqueará o `DELETE` (resultando em HTTP 500).
3. **Atualizações Perdidas (Lost Updates):**
   - Nenhuma entidade possui controle de concorrência otimista (`@Version` em `DefaultEntity`). Duas edições simultâneas do mesmo `Produto` (ex.: alteração de `estoque`) ou `Cliente` sobrescrevem silenciosamente uma à outra (last-write-wins).

---

## 4. Auditoria de Testes e Cruzamento com o Catálogo de Regras

### 4.1. O que Existe de Fato
- **Back-end:** 4 classes de teste unitário em `src/test/java/br/unitins/topicos1/aurafit/service/` totalizando **21 métodos `@Test`** (mais 1 helper `Entidades.java`).
  - Os testes instanciam `*ServiceImpl` diretamente com `new` e injetam repositórios fake em memória.
  - **Importante:** Como não usam `@QuarkusTest` nem container CDI, **nenhuma anotação de Bean Validation (`@NotBlank`, `@Length`, `@CPF`, `@CNPJ`, `@Email`, `@Past`, `@DecimalMin`, `@Digits`) é exercitada pelos testes existentes!**
- **Front-end:** **0 arquivos de teste** (`ng test` falha por não encontrar nenhum `*.spec.ts`).

### 4.2. Matriz de Cobertura de Testes por Regra de Negócio (`RN-001` a `RN-019`)

| ID da Regra | Resumo da Regra | Testada? | Onde é Testada / Lacuna Real |
|---|---|---|---|
| **RN-001** | Nome de Categoria obrigatório (2..100) e único | **Parcial** | Unicidade testada em `CategoriaServiceTest.deveRejeitarNomeDuplicado`. Tamanho (`2..100`) e `@NotBlank` **não testados** (dependem de Bean Validation/CDI). |
| **RN-002** | Categoria com produtos não pode ser excluída | **Sim** | `CategoriaServiceTest.naoDeveExcluirCategoriaComProdutos`. |
| **RN-003** | Nome de Sabor obrigatório (2..80) e único | **NÃO** | **Nenhum teste existe para `SaborServiceImpl`.** |
| **RN-004** | Sabor com produtos não pode ser excluído | **NÃO** | **Nenhum teste existe para `SaborServiceImpl`.** |
| **RN-005** | CNPJ de Marca obrigatório (14 dígitos, `@CNPJ`) e único | **Parcial** | Unicidade testada em `MarcaServiceTest.deveRejeitarCnpjDuplicado`. Dígitos verificadores `@CNPJ` **não testados**. |
| **RN-006** | Marca com produtos não pode ser excluída | **Sim** | `MarcaServiceTest.naoDeveExcluirMarcaComProdutos` e `deveExcluirMarcaSemProdutos`. |
| **RN-007** | CNPJ e Telefone de Fornecedor obrigatórios e CNPJ único | **NÃO** | **Nenhum teste existe para `FornecedorServiceImpl`.** |
| **RN-008** | Fornecedor com produtos não pode ser excluído | **NÃO** | **Nenhum teste existe para `FornecedorServiceImpl`.** |
| **RN-009** | Produto novo/trocado não pode usar Marca inativa; edição mantendo marca inativa é permitida | **Sim** | `ProdutoServiceTest.deveRejeitarMarcaInativaEmProdutoNovo` e `devePermitirManterMarcaInativaAoEditar`. |
| **RN-010** | Campos obrigatórios, preço/peso/estoque e vínculos de Produto | **Parcial** | Vínculos de `TipoProteina` e `Sabor` testados em `ProdutoServiceTest.deveRejeitarTipoProteinaESaborInexistentes`. Inexistência de `Categoria`, `Marca` e `Fornecedor`, bem como limites de `preco`, `pesoGramas` e `estoque`, **não testados**. |
| **RN-011** | Sabores do Produto retornados em ordem alfabética | **Sim** | `ProdutoServiceTest.deveCadastrarProdutoResolvendoRelacionamentos` (L114). |
| **RN-012** | Idade mínima de 16 anos para Cliente | **Sim** | `ClienteServiceTest.deveRejeitarClienteMenorQueIdadeMinima`. |
| **RN-013** | CPF e e-mail únicos e válidos em Pessoa | **Parcial** | CPF duplicado testado em `ClienteServiceTest.deveRejeitarCpfDuplicado`. **E-mail duplicado (`findByEmailExceptId`) e validação de dígitos `@CPF` NÃO possuem teste.** |
| **RN-014** | Cliente exige ao menos 1 telefone e 1 endereço | **Parcial** | Substituição de telefones no update testada em `ClienteServiceTest.deveAtualizarSubstituindoTelefones`. Rejeição de lista de telefones vazia (`@NotEmpty`) ou endereço nulo (`@NotNull`) **não testada**. |
| **RN-015** | Localização/auto-cadastro de Município via IBGE ou nome+UF | **Parcial** | Busca por IBGE, criação de novo município e UF inexistente testados em `ClienteServiceTest`. **Conflito de UF em município existente (L63-L65 de `MunicipioServiceImpl`) e preenchimento de IBGE faltante (L66-L68) NÃO possuem teste.** |
| **RN-016** | Exclusão de Cliente exclui Pessoa associada | **NÃO** | **`ClienteServiceImpl.delete` e `findByCpf` não possuem nenhum teste.** |
| **RN-017** | Limites de paginação (`page >= 0`, `1 <= pageSize <= 100`) | **Sim** | `MarcaServiceTest.deveRejeitarPaginacaoInvalida`. |
| **RN-018** | Normalização `trim()`, vazio → `null` e e-mail `toLowerCase()` | **Sim** | Testada em `CategoriaServiceTest`, `MarcaServiceTest` e `ClienteServiceTest`. |
| **RN-019** | Regras do carrinho e frete grátis (`>= R$ 199`) no Front-end | **NÃO** | **Nenhum teste existe no front-end.** |

---

## 5. Qualidade do Código

- **Tamanho e Coesão:** Classes pequenas, coesas e muito legíveis. O maior arquivo Java tem 160 linhas (`ProdutoServiceImpl.java`) e o maior arquivo TypeScript tem 264 linhas (`cliente-form.ts`).
- **Pendências (`TODO`/`FIXME`):** Zero ocorrências em todo o projeto.
- **Duplicação de Código:**
  - Método privado `paginar(Supplier<PanacheQuery<E>> consulta, int page, int pageSize)` está copiado identicamente nas 8 classes `*ServiceImpl`.
  - No front-end, a estrutura de listagem com debounce de 300ms (`buscarPorNome`, `mudarPagina`, `excluir` com retrocesso de página) repete-se nas 6 classes `*-list.ts`, e a estrutura dos services CRUD repete-se em `categoria.service.ts`, `marca.service.ts`, `sabor.service.ts`, `fornecedor.service.ts` e `cliente.service.ts`.
