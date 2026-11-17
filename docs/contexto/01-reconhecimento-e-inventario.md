# 01 — Reconhecimento e Inventário do Projeto

Este documento consolida o levantamento da Fase 0 da auditoria do projeto **AuraFit**, incluindo stack tecnológica real vs. documentada, estado do repositório, resultado de compilação e testes em ambiente local, e o inventário completo dos arquivos do workspace.

---

## 1. Stack Tecnológica e Infraestrutura Descobertas

### 1.1. Visão Geral do Workspace
O workspace (`c:\Users\AVIS 01\Documents\Raphael\AuraFit-main\AuraFit-main`) é composto por dois subprojetos independentes (monorepo por diretório, sem arquivo agregador na raiz):
1. **`aurafit-main/`**: API REST back-end da loja de suplementos AuraFit (disciplina Tópicos 2 — UNITINS).
2. **`aurafit-frontend/`**: Aplicação SPA front-end administrativa e protótipo de loja virtual.

### 1.2. Back-end (`aurafit-main`)
| Item | Valor Confirmado no Código | Fonte / Evidência |
|---|---|---|
| **Linguagem (Configurada no Build)** | Java 21 (`<maven.compiler.release>21</maven.compiler.release>`) | [`pom.xml`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/pom.xml#L15) |
| **Linguagem (Documentada / Docker)** | Java 25 no README e `openjdk-25-runtime:1.24` nos Dockerfiles JVM | [`README.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/README.md#L6), [`Dockerfile.jvm`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/docker/Dockerfile.jvm#L83) |
| **Framework Principal** | Quarkus `3.38.3` (`io.quarkus.platform:quarkus-bom`) | [`pom.xml`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/pom.xml#L18-L20) |
| **Gerenciador de Build** | Apache Maven (`pom.xml` + Maven Wrapper `3.9.14` configurado em `.mvn/wrapper/maven-wrapper.properties`) | [`maven-wrapper.properties`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/.mvn/wrapper/maven-wrapper.properties#L1-L4) |
| **Persistência / ORM** | Hibernate ORM com Panache (`quarkus-hibernate-orm-panache`) | [`pom.xml`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/pom.xml#L56-L63) |
| **Banco de Dados** | PostgreSQL (`quarkus-jdbc-postgresql`), banco local `topicos2db` (`localhost:5432`) | [`application.properties`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/application.properties#L2-L5) |
| **Estratégia de Schema** | `drop-and-create` + carga automática via `import.sql` (sem Flyway/Liquibase) | [`application.properties`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/application.properties#L7) |
| **Validação** | Jakarta Bean Validation / Hibernate Validator (`quarkus-hibernate-validator`) | [`pom.xml`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/pom.xml#L72-L75) |
| **Documentação de API** | SmallRye OpenAPI / Swagger UI (`quarkus-smallrye-openapi`) | [`pom.xml`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/pom.xml#L50-L53) |
| **Testes** | JUnit 5 (`quarkus-junit`), REST Assured (`rest-assured`, declarado mas não utilizado) | [`pom.xml`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/pom.xml#L84-L93) |
| **Filas / Mensageria / Cache** | Não encontrado (inexistente no projeto) | [`pom.xml`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/pom.xml) |
| **CI / CD** | Não encontrado (sem `.github/workflows`, `.gitlab-ci.yml` ou `Jenkinsfile`) | Raiz do workspace |

### 1.3. Front-end (`aurafit-frontend`)
| Item | Valor Confirmado no Código | Fonte / Evidência |
|---|---|---|
| **Framework Principal** | Angular `^22.1.0` + Angular Material `^22.2.1` + Angular CDK `^22.2.1` | [`package.json`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/package.json#L12-L24) |
| **Linguagem** | TypeScript `~6.0.2` (com `strict: true`) | [`package.json`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/package.json#L31), [`tsconfig.json`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/tsconfig.json#L6) |
| **Gerenciador de Pacotes / Build** | npm (`package-lock.json` lockfileVersion 3) + `@angular/build:application` (`^22.1.5`) | [`angular.json`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/angular.json#L17-L18) |
| **Runner de Testes Configurado** | Vitest `^4.0.8` + `jsdom` `^28.0.0` via `@angular/build:unit-test` | [`package.json`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/package.json#L29-L32), [`angular.json`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/angular.json#L66-L68) |
| **Serviços Externos Consumidos** | API Back-end (`http://localhost:8080`) e API pública ViaCEP (`https://viacep.com.br/ws/{cep}/json/`) | [`api.config.ts`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/api.config.ts#L2), [`cep.service.ts`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/services/cep.service.ts#L14) |

---

## 2. Estado do Controle de Versão (Git)

- **Confirmado:** Não existe diretório `.git` na raiz do workspace nem dentro de `aurafit-main/` ou `aurafit-frontend/` (`fatal: not a git repository`).
- O projeto foi disponibilizado como um snapshot exportado (provavelmente de arquivo `.zip` da branch `main`, dado o sufixo `-main` nos diretórios), portanto não há histórico de commits ou branches localizáveis.
- Ambos os subprojetos possuem arquivos `.gitignore` configurados ([`aurafit-main/.gitignore`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/.gitignore) e [`aurafit-frontend/.gitignore`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/.gitignore)).

---

## 3. Verificação de Compilação e Execução de Testes

Os comandos de verificação foram executados em ambiente local seguro (sandboxed, sem tocar no banco PostgreSQL):

| Subprojeto | Comando Executado | Resultado | Detalhes / Observações Confirmadas |
|---|---|---|---|
| **Back-end** | `mvnw.cmd -v` | **FALHA (Offline)** | O script `mvnw.cmd` tenta baixar `maven-wrapper.jar` ou a distribuição Maven `3.9.14` de `repo.maven.apache.org` porque `.mvn/wrapper/maven-wrapper.jar` não está versionado. |
| **Back-end** | `mvn test-compile` (Maven global 3.9.16, JDK 21.0.12.1) | **SUCESSO (`BUILD SUCCESS`)** | Todas as 92 classes de produção e 5 classes de teste compilam sem erros nem warnings. |
| **Back-end** | `mvn test` | **FALHA (Resolução de Plugin Offline)** | `pom.xml` define `<surefire-plugin.version>3.5.6</surefire-plugin.version>` para `maven-surefire-plugin` e `maven-failsafe-plugin`. No repositório local `~/.m2`, existe `maven-surefire-plugin:3.5.2` e `maven-failsafe-plugin:3.5.6`. Sem rede externa no sandbox, o Maven falha ao tentar baixar `maven-surefire-plugin:3.5.6`. |
| **Front-end** | `npx tsc --noEmit` | **SUCESSO (Código 0)** | Checagem estática de tipos TypeScript (`strict: true`) passa com zero erros em todos os componentes, serviços e modelos. |
| **Front-end** | `npx ng build --configuration development` | **SUCESSO (Código 0)** | Bundle gerado com sucesso (`main.js` 4.03 MB, `styles.css` 17.03 kB). |
| **Front-end** | `npm run build` (`production`) | **FALHA (Offline)** | Em modo `production`, o Angular tenta fazer inlining de Google Fonts (`https://fonts.googleapis.com/css2?family=Manrope...`) referenciada em `src/index.html` e falha sem acesso à internet (`ENOTFOUND fonts.googleapis.com`). |
| **Front-end** | `npx ng test --watch=false` | **FALHA (Sem Testes)** | `Error: No tests found matching the following patterns: - Included: **/*.spec.ts, **/*.test.ts`. Não há nenhum arquivo de teste no front-end. |

---

## 4. Inventário Completo de Arquivos

Total de arquivos auditados (excluindo diretórios gerados `node_modules/`, `target/`, `.angular/` e `dist/`): **193 arquivos** (112 no back-end e 81 no front-end).

### 4.1. Back-end (`aurafit-main`) — 112 arquivos

#### Configuração, Build, Documentação e Scripts (11 arquivos)
| Arquivo | Bytes | Linhas | Papel e Relevância |
|---|---|---|---|
| `aurafit-main/pom.xml` | 5.900 | 146 | Alta — Manifesto Maven, dependências Quarkus 3.38.3 e Java 21. |
| `aurafit-main/README.md` | 2.717 | 43 | Alta — Documentação de execução, estrutura, endpoints e regras de negócio. |
| `aurafit-main/src/main/resources/application.properties` | 1.061 | 21 | Alta — Configuração JDBC PostgreSQL, `drop-and-create`, CORS e `problem.base-url`. Contém credencial local de dev. |
| `aurafit-main/src/main/resources/import.sql` | 12.342 | 102 | Alta — Carga inicial (27 Estados, 10 Municípios, 5 Categorias, 5 Marcas, 3 Fornecedores, 5 Sabores, 8 Produtos, 3 Clientes). |
| `aurafit-main/docs/diagrama-classes.puml` | 2.082 | 107 | Média — Diagrama PlantUML do modelo de domínio. |
| `aurafit-main/docs/Diagrama_aurafit.png` | 34.281 | bin | Baixa — Imagem renderizada do diagrama de classes. |
| `aurafit-main/.mvn/wrapper/maven-wrapper.properties` | 255 | 4 | Baixa — Configura distribuição Maven 3.9.14. |
| `aurafit-main/mvnw` / `mvnw.cmd` | 20.082 | 428 | Baixa — Scripts do Maven Wrapper. |
| `aurafit-main/.gitignore` / `.dockerignore` | 498 | 41 | Baixa — Exclusões de controle de versão e imagem Docker. |

#### Dockerfiles (`src/main/docker/` — 4 arquivos)
| Arquivo | Bytes | Linhas | Papel e Relevância |
|---|---|---|---|
| `Dockerfile.jvm` | 5.669 | 94 | Baixa — Template padrão Quarkus JVM (`ubi9/openjdk-25-runtime:1.24`). |
| `Dockerfile.legacy-jar` | 5.482 | 91 | Baixa — Template padrão Quarkus Legacy JAR. |
| `Dockerfile.native` | 841 | 27 | Baixa — Template padrão Quarkus Native (`ubi-minimal:9.7`). |
| `Dockerfile.native-micro` | 1.077 | 30 | Baixa — Template padrão Quarkus Native Micro. |

#### Modelo de Domínio e Converters (`src/main/java/.../model/` — 16 arquivos)
| Arquivo | Bytes | Linhas | Papel e Relevância |
|---|---|---|---|
| `DefaultEntity.java` | 1.271 | 42 | Alta — Superclasse `@MappedSuperclass` com `id`, `dataCadastro` e `dataAlteracao`. |
| `Produto.java` | 3.329 | 111 | Alta — Entidade central do catálogo e estoque. |
| `Pessoa.java` | 2.518 | 82 | Alta — Dados pessoais, composição de `Telefone` (`1..*`) e `Endereco` (`1:1`). |
| `Endereco.java` | 1.669 | 58 | Alta — Endereço físico vinculado a `Municipio`. |
| `Cliente.java` | 1.194 | 36 | Alta — Papel de cliente vinculado a `Pessoa` (`@OneToOne`). |
| `Telefone.java` | 1.173 | 41 | Média — Telefone de `Pessoa` (`codigoArea`, `numero`, `whatsapp`). |
| `Fornecedor.java` | 1.133 | 40 | Média — Fornecedor (`razaoSocial`, `cnpj`, `telefone`, `email`). |
| `Marca.java` | 1.065 | 39 | Alta — Marca (`nome`, `cnpj`, `emailContato`, `ativa`). |
| `Municipio.java` | 1.013 | 34 | Alta — Município (`nome`, `codigoIbge`, `Estado`). |
| `TipoProteina.java` | 917 | 34 | Média — Enum (`CONCENTRADO`..`VEGETAL`). |
| `Regiao.java` | 881 | 34 | Baixa — Enum (`CENTRO_OESTE`..`SUL`). |
| `Estado.java` | 788 | 30 | Média — UF brasileira (`nome`, `sigla`, `regiao`). |
| `Categoria.java` | 604 | 22 | Média — Categoria de produto (`nome`, `descricao`). |
| `Sabor.java` | 599 | 22 | Média — Sabor de produto (`nome`, `descricao`). |
| `converterjpa/TipoProteinaConverter.java` | 571 | 15 | Média — Converte `TipoProteina` <-> `Long` no banco. |
| `converterjpa/RegiaoConverter.java` | 541 | 15 | Média — Converte `Regiao` <-> `Long` no banco. |

#### DTOs (`src/main/java/.../dto/` — 19 arquivos)
| Arquivo | Bytes | Linhas | Papel e Relevância |
|---|---|---|---|
| `ClienteRequestDTO.java` | 1.786 | 40 | Alta — Entrada de cliente + pessoa com Bean Validation. |
| `ProdutoRequestDTO.java` | 1.733 | 39 | Alta — Entrada de produto com Bean Validation. |
| `EnderecoRequestDTO.java` | 1.546 | 33 | Alta — Entrada de endereço com cidade, UF e código IBGE. |
| `FornecedorRequestDTO.java` | 1.050 | 22 | Média — Entrada de fornecedor (`@CNPJ`, telefone 10..11 dígitos). |
| `MarcaRequestDTO.java` | 976 | 22 | Média — Entrada de marca (`@CNPJ`, `ativa`). |
| `PageResponse.java` | 662 | 21 | Alta — Envelope genérico de paginação `{ items, page, pageSize, totalItems, totalPages }`. |
| `TelefoneRequestDTO.java` | 624 | 15 | Média — Entrada de telefone (`codigoArea`, `numero`, `whatsapp`). |
| `ProdutoResponseDTO.java` | 497 | 19 | Média — Saída de produto com objetos aninhados. |
| `CategoriaRequestDTO.java` / `SaborRequestDTO.java` | 928 | 22 | Média — Entradas de categoria e sabor. |
| `ClienteResponseDTO.java` | 433 | 18 | Média — Saída achatada de `Cliente` + `Pessoa`. |
| Demais `ResponseDTO` (8 arquivos) | 1.341 | 66 | Baixa — Records imutáveis de resposta. |

#### Exceções e Tratamento RFC 7807 (`src/main/java/.../exception/` — 9 arquivos)
| Arquivo | Bytes | Linhas | Papel e Relevância |
|---|---|---|---|
| `ConstraintViolationExceptionMapper.java` | 2.414 | 54 | Alta — Formata violações do Hibernate Validator em `errors: [{ field, message }]` compatíveis com Angular `form.get()`. |
| `UncaughtExceptionMapper.java` | 1.768 | 34 | Alta — Fallback global para `Throwable` (preserva status `< 500` de `WebApplicationException`, retorna 500 genérico nos demais). |
| `ProblemFactory.java` / `Problem.java` | 1.756 | 48 | Alta — Construção do payload `application/problem+json`. |
| `NotFoundExceptionMapper.java` | 860 | 22 | Média — Mapeia rotas JAX-RS inexistentes para 404 RFC 7807. |
| `ValidationExceptionMapper.java` / `ValidationException.java` | 1.502 | 37 | Alta — Exceção de regra de negócio/validação customizada (HTTP 400). |
| `ResourceNotFoundExceptionMapper.java` / `ResourceNotFoundException.java` | 1.073 | 28 | Média — Exceção de entidade não encontrada (HTTP 404). |

#### Mappers (`src/main/java/.../mapper/` — 10 arquivos)
- `CategoriaMapper.java`, `ClienteMapper.java`, `EnderecoMapper.java`, `EstadoMapper.java`, `FornecedorMapper.java`, `MarcaMapper.java`, `MunicipioMapper.java`, `ProdutoMapper.java`, `SaborMapper.java`, `TelefoneMapper.java` (total: 6.107 bytes, 150 linhas). Relevância: **Média**.

#### Repositórios (`src/main/java/.../repository/` — 9 arquivos)
- `ProdutoRepository.java` (2.477 B, 59 L), `MunicipioRepository.java` (935 B, 21 L), `PessoaRepository.java` (920 B, 22 L), `CategoriaRepository.java` (906 B, 21 L), `FornecedorRepository.java` (900 B, 21 L), `SaborRepository.java` (882 B, 21 L), `MarcaRepository.java` (835 B, 21 L), `ClienteRepository.java` (743 B, 18 L), `EstadoRepository.java` (743 B, 18 L). Relevância: **Alta**.

#### Resources REST (`src/main/java/.../resource/` — 11 arquivos)
- `ProdutoResource.java`, `ClienteResource.java`, `FornecedorResource.java`, `CategoriaResource.java`, `SaborResource.java`, `MarcaResource.java`, `EstadoResource.java`, `MunicipioResource.java`, `TipoProteinaResource.java`, `RegiaoResource.java`, `RootResource.java` (total: 17.821 bytes, 467 linhas). Relevância: **Alta**.

#### Services (`src/main/java/.../service/` — 18 arquivos)
- 8 interfaces (`*Service.java`), 8 implementações (`*ServiceImpl.java`), `Paginacao.java` e `Textos.java` (total: 39.585 bytes, 822 linhas). Relevância: **Alta** (concentram todas as regras de negócio do back-end).

#### Testes Unitários (`src/test/java/...` — 5 arquivos)
- `Entidades.java` (346 B, 12 L), `CategoriaServiceTest.java` (2.877 B, 70 L), `ClienteServiceTest.java` (7.033 B, 155 L), `MarcaServiceTest.java` (4.672 B, 115 L), `ProdutoServiceTest.java` (5.593 B, 129 L). Relevância: **Alta**.

---

### 4.2. Front-end (`aurafit-frontend`) — 81 arquivos

- **Configuração e Raiz (`12 arquivos`)**: `package.json`, `package-lock.json`, `angular.json`, `tsconfig.json`, `tsconfig.app.json`, `tsconfig.spec.json`, `.prettierrc`, `.editorconfig`, `.gitignore`, `.vscode/extensions.json`, `README.md`, `public/favicon.svg`.
- **Bootstrap e Estilos Globais (`8 arquivos`)**: `src/index.html`, `src/main.ts`, `src/material-theme.scss`, `src/styles.css`, `src/app/api.config.ts`, `src/app/app.config.ts`, `src/app/app.routes.ts`, `src/app/app.ts`.
- **Models (`11 arquivos` em `src/app/models/`)**: `categoria.model.ts`, `cliente.model.ts`, `endereco-viacep.model.ts`, `estado.model.ts`, `fornecedor.model.ts`, `marca.model.ts`, `pagina.model.ts`, `problema-api.model.ts`, `produto.model.ts`, `sabor.model.ts`, `tipo-proteina.model.ts`.
- **Services (`10 arquivos` em `src/app/services/`)**: `carrinho.service.ts`, `categoria.service.ts`, `cep.service.ts`, `cliente.service.ts`, `estado.service.ts`, `fornecedor.service.ts`, `http-params.ts`, `marca.service.ts`, `produto.service.ts`, `sabor.service.ts`, `sessao.service.ts`.
- **Shared Utilities (`7 arquivos` em `src/app/shared/`)**: `confirmacao-dialog.ts`, `erros-formulario.ts`, `formato.pipe.ts`, `formatos.ts`, `notificacao.service.ts`, `paginador-pt-br.ts`, `validadores.ts`.
- **Componentes Admin (`33 arquivos` em `src/app/components/admin/`)**: `admin-layout` (3), `painel` (3), `categorias` (4), `clientes` (5), `fornecedores` (4), `marcas` (4), `produtos` (4), `sabores` (4).
- **Componentes Loja — Protótipo (`18 arquivos` em `src/app/components/loja/`)**: `loja-layout` (3), `catalogo` (3), `carrinho` (3), `pagamento` (3), `login` (3), `perfil` (3).

---

## 5. Código Gerado, Código Morto Aparente e Recursos Não Utilizados

1. **Dependência `rest-assured` declarada mas não utilizada**:
   - Em [`pom.xml`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/pom.xml#L89-L93), `io.rest-assured:rest-assured` está declarado no escopo `test`, mas não há nenhum teste de endpoint REST no projeto.
2. **Endpoints do Back-end nunca consumidos pelo Front-end**:
   - `GET /clientes/cpf/{cpf}` ([`ClienteResource.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/resource/ClienteResource.java#L49-L53)): implementado no Resource, Service e Repository, mas `ClienteService` no Angular não possui método para chamá-lo.
   - `GET /produtos/nome/{nome}` ([`ProdutoResource.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/resource/ProdutoResource.java#L43-L49)) e `ProdutoService.findAll(page, pageSize)` ([`ProdutoServiceImpl.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/ProdutoServiceImpl.java#L47-L51)): `ProdutoResource.buscarTodos` chama diretamente `service.buscar(...)`, tornando `ProdutoService.findAll(page, pageSize)` **código morto** (nunca chamado por nenhum Resource nem teste).
   - Todos os endpoints de `MunicipioResource` (`GET /municipios`, `/municipios/nome/{nome}`, `/municipios/{id}`), `RegiaoResource` (`GET /regioes`, `/regioes/{id}`), `EstadoResource` (`GET /estados/{id}`, `/estados/sigla/{sigla}`, `/estados/nome/{nome}`) e `TipoProteinaResource` (`GET /tipos-proteina/{id}`) não são consumidos pelo front-end.
3. **Cabeçalhos CORS expostos mas nunca emitidos pelo Back-end**:
   - [`application.properties`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/application.properties#L20) expõe `X-Page, X-Page-Size, X-Total-Count`, resquício de paginação via headers HTTP; o sistema real trafega paginação exclusivamente no corpo JSON via `PageResponse<T>`.
