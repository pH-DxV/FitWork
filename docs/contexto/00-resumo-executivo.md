# 00 — Resumo Executivo da Auditoria de Contexto

## 1. Resumo Executivo (Visão Geral e Saúde do Sistema)

O **AuraFit** é um sistema de gestão e comércio eletrônico de suplementos alimentares (disciplina Tópicos 2 — UNITINS) dividido em dois subprojetos: uma API RESTful em **Quarkus 3.38.3 / Java 21 / PostgreSQL** (`aurafit-main`) e uma SPA em **Angular 22 / Angular Material 3** (`aurafit-frontend`). O back-end implementa 6 módulos CRUD completos (`Categoria`, `Marca`, `Fornecedor`, `Sabor`, `Produto`, `Cliente/Pessoa`) e 4 módulos de consulta (`Estado`, `Municipio`, `TipoProteina`, `Regiao`), com paginação padronizada, tratamento de erros RFC 7807 (`application/problem+json`) e auto-cadastro de municípios via integração front-end com o **ViaCEP**. O front-end entrega toda a área administrativa funcional (`/admin/*`) integrada à API e um protótipo navegável da loja (`/loja/*`, onde apenas o catálogo consome a API real e carrinho/login/pagamento operam em memória/`localStorage`). A saúde estrutural e a legibilidade do código são excelentes (100% do código compila sem erros de tipo e sem `TODO`/`FIXME`), mas o sistema ainda não possui autenticação/autorização (prevista para a próxima etapa) e apresenta pontos de atenção em configuração de banco (`drop-and-create`), lacunas de testes e pequenos desvios de comportamento entre front-end e back-end.

---

## 2. Top 10 Achados Mais Relevantes

1. **[Crítico | CONFIRMADO] `ACH-01` — Ausência de autenticação e autorização:** Todos os 42 endpoints REST (incluindo `GET /clientes` com CPF/endereço e `DELETE`) e as rotas `/admin/*` estão abertos sem proteção JWT/roles (previsto para a próxima etapa da disciplina).
2. **[Alto | CONFIRMADO] `ACH-02` — `drop-and-create` sem restrição de perfil e credenciais em texto claro:** [`application.properties`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/resources/application.properties#L3-L13) recria o banco a cada start sem prefixo `%dev.`, expõe credenciais locais e loga parâmetros SQL com dados pessoais.
3. **[Alto | CONFIRMADO] `ACH-03` — Exclusão de `Cliente` apaga `Pessoa` incondicionalmente:** [`ClienteServiceImpl.delete`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/ClienteServiceImpl.java#L92-L98) exclui fisicamente `Pessoa`, conflitando com o compartilhamento planejado de `Pessoa` com `Usuario`/`Funcionario`.
4. **[Alto | CONFIRMADO] `ACH-04` — Produtos inativos aparecem na vitrine da loja:** Embora [`produto-form.html:107`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/admin/produtos/produto-form/produto-form.html#L107) afirme *"Produto ativo (visível na loja)"*, `GET /produtos` não filtra ativos e [`Catalogo`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/catalogo/catalogo.ts#L67-L86) exibe produtos inativos como `"Indisponível"`.
5. **[Médio | CONFIRMADO] `ACH-05` — Limite fixo de 100 itens nos selects de relacionamento:** `ProdutoForm`, `ProdutoList` e `Catalogo` buscam `listar('', 0, 100)`; registros além do 100º nunca aparecem nos `<mat-select>`.
6. **[Médio | CONFIRMADO] `ACH-06` — Validação `@Length(min=2)` antes de `.trim()`:** `CategoriaForm` e `SaborForm` não fazem `.trim()` no envio, permitindo que `" a "` passe na validação e seja salvo como `"a"` (1 char) após `.trim()` no Service.
7. **[Médio | CONFIRMADO] `ACH-07` — Consultas N+1 em `Produto` e `Cliente`:** Listagens paginadas carregam `Produto.sabores`, `Cliente.pessoa` e `Pessoa.telefones` de forma `LAZY` item a item no Mapper, sem `JOIN FETCH` ou `@BatchSize`.
8. **[Médio | CONFIRMADO] `ACH-08` — Falhas de execução offline em testes e build de produção:** Divergência no cache local entre `maven-surefire-plugin` (`3.5.2`) e `maven-failsafe-plugin` (`3.5.6`), falta de `maven-wrapper.jar` e inlining online de Google Fonts no `ng build`.
9. **[Médio | CONFIRMADO] `ACH-09` — Cobertura de testes parcial:** Front-end sem arquivos `*.spec.ts`; no back-end (21 testes unitários puros sem CDI), Bean Validation (`@CPF`, `@CNPJ`, `@Valid`), `FornecedorServiceImpl`, `SaborServiceImpl` e `ClienteServiceImpl.delete` não são testados.
10. **[Baixo | CONFIRMADO] `ACH-10` / `ACH-11` — Detalhes de UX no Front-end:** Sobrescrita de logradouro/bairro em CEPs gerais no `ClienteForm`, falta de validação de 2 casas decimais em `preco`, typo em `sabor-form.html:4` (`"Voltar paro sabores"`) e loop no link `"Criar conta"` da loja.

---

## 3. O Que Está Bem Feito e Deve Ser Preservado

- **Arquitetura de Erros RFC 7807 + Integração Angular Forms:** `ProblemFactory` + `ConstraintViolationExceptionMapper` no back-end e `aplicarErrosDaApi` em `erros-formulario.ts` no front-end conectam erros de validação diretamente aos campos do formulário (inclusive tratando campos `disabled`).
- **Persistência de Enums por ID (`AttributeConverter`):** `TipoProteinaConverter` e `RegiaoConverter` garantem integridade no banco independente da ordem ou nome das constantes Java.
- **Regra de Edição de Produto com Marca Inativa:** `ProdutoServiceImpl` bloqueia marcas inativas para novos produtos ou trocas, mas permite editar produtos legados mantendo sua marca inativa.
- **Fluxo ViaCEP + Auto-Cadastro de Município:** Travamento de cidade/UF no `ClienteForm` para preservar coerência com o `codigoIbge` e resolução/criação transacional em `MunicipioServiceImpl.buscarOuCriar`.

---

## 4. Índice do Pacote de Contexto (`docs/contexto/`)

1. [`00-resumo-executivo.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/00-resumo-executivo.md) — Este documento.
2. [`01-reconhecimento-e-inventario.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/01-reconhecimento-e-inventario.md) — Stack, ambiente, compilação/testes e inventário completo dos 193 arquivos.
3. [`02-arquitetura.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/02-arquitetura.md) — Camadas, diagrama Mermaid, convenções, 42 rotas REST e acoplamentos.
4. [`03-dominio-e-modelo.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/03-dominio-e-modelo.md) — Glossário, entidades, diagramas de classes e ER (Mermaid), máquinas de estado e divergências código × banco.
5. [`04-catalogo-de-regras-de-negocio.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/04-catalogo-de-regras-de-negocio.md) — Catálogo `RN-001` a `RN-019` com evidências, regras implícitas e duplicações.
6. [`05-fichas-de-fluxos.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/05-fichas-de-fluxos.md) — Fichas detalhadas de ponta a ponta dos 6 fluxos principais.
7. [`06-analise-cruzada.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/06-analise-cruzada.md) — Comparativo entre os CRUDs, inconsistências e fluxos interrompidos.
8. [`07-dados-integracoes-infra.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/07-dados-integracoes-infra.md) — Schema, `import.sql`, índices faltantes, N+1, transações, ViaCEP e Docker.
9. [`08-qualidade-seguranca-testes.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/08-qualidade-seguranca-testes.md) — Segurança, concorrência e matriz de cobertura de testes × regras de negócio.
10. [`09-achados-priorizados.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/09-achados-priorizados.md) — Detalhamento de `ACH-01` a `ACH-11` e pontos a preservar.
11. [`10-mapa-de-cobertura.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/10-mapa-de-cobertura.md) — Cobertura de leitura (100%) e matriz do que foi executado vs. lido.
12. [`11-perguntas-em-aberto.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/11-perguntas-em-aberto.md) — 5 questões de negócio/intenção (`Q-01` a `Q-05`) com opções e recomendações.
13. [`12-briefing-para-proximos-agentes.md`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/docs/contexto/12-briefing-para-proximos-agentes.md) — Guia rápido e autossuficiente para agentes futuros.
