# 06 — Análise Cruzada entre Fluxos e Recursos

Este documento apresenta a análise cruzada dos fluxos, identificando operações similares com comportamentos divergentes, inconsistências entre recursos parecidos, fluxos que se contradizem ou se interrompem, e dependências ocultas.

---

## 1. Comparativo de Consistência entre os 6 Recursos CRUD

| Aspecto | Categoria | Sabor | Marca | Fornecedor | Produto | Cliente |
|---|---|---|---|---|---|---|
| **Campo de busca textual (`/nome/{nome}`)** | `nome` | `nome` | `nome` | `razaoSocial` (via rota `/nome/{nome}`) | `nome` (também via `?nome=`) | `pessoa.nome` |
| **Possui flag `ativo`/`ativa`?** | Não | Não | Sim (`ativa`) | **Não** | Sim (`ativo`) | Sim (`ativo`) |
| **Exclusão bloqueada por vínculo?** | Sim (`Produto`) | Sim (`Produto`) | Sim (`Produto`, sugere inativar) | Sim (`Produto`, **sem opção de inativar**) | Não (exclusão livre) | Não (exclui `Cliente` + `Pessoa` + `Telefone` + `Endereco`) |
| **`@Transactional` nos métodos `GET` do Service?** | Não | Não | Não | Não | **Sim** (todos os GETs) | **Sim** (todos os GETs) |
| **Front-end faz `.trim()` antes do envio HTTP?** | **Não** | **Não** | Sim | Sim | Sim | Sim |
| **Exibe `dataCadastro` no `ResponseDTO`?** | Não | Não | Não | Não | Não | **Sim** |
| **Possui testes unitários no Back-end?** | Sim (3) | **Não (0)** | Sim (7) | **Não (0)** | Sim (5) | Sim (6) |

---

## 2. Operações Duplicadas com Comportamentos Ligeiramente Diferentes

### 2.1. Consulta de CEP em `ClienteForm` vs. `Pagamento`
A consulta ao ViaCEP (`CepService.consultar`) foi implementada em dois lugares diferentes do front-end com comportamentos distintos:
- Em [`ClienteForm.buscarCep()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/admin/clientes/cliente-form/cliente-form.ts#L143-L188):
  - Evita consultar novamente o mesmo CEP (`cep === this.ultimoCepConsultado`).
  - Preenche `complemento` se o campo estiver vazio (`this.endereco.controls.complemento.value || resposta.complemento`).
  - Preenche `codigoIbge` e **desabilita** `cidade` e `uf` (`disable()`), oferecendo botão `"Editar cidade/UF"`.
- Em [`Pagamento.buscarCep()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/pagamento/pagamento.ts#L71-L96):
  - Não possui cache de `ultimoCepConsultado` (consulta toda vez que o evento dispara).
  - Não preenche `complemento`.
  - Não desabilita `cidade` e `uf`.

### 2.2. Busca de Produtos por Nome: `GET /produtos?nome=X` vs. `GET /produtos/nome/{nome}`
No back-end ([`ProdutoResource.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/resource/ProdutoResource.java#L34-L49)), existem dois caminhos para buscar produtos por nome:
1. `GET /produtos?nome=X` → chama `ProdutoService.buscar` → `ProdutoRepository.buscar`: ignora `nome` se for `null` ou `isBlank()` (`if (nome != null && !nome.isBlank())`).
2. `GET /produtos/nome/{nome}` → chama `ProdutoService.findByNome` → `ProdutoRepository.findByNome`: executa diretamente `"%" + nome.trim() + "%"`.
- O front-end (`ProdutoService` no Angular) usa **apenas** `GET /produtos?nome=...`, enquanto todos os outros 5 services do Angular (`CategoriaService`, `MarcaService`, `SaborService`, `FornecedorService`, `ClienteService`) usam `GET /{recurso}/nome/{termo}` quando há filtro textual.

### 2.3. Modelagem e Formatação de Telefone: `Fornecedor` vs. `Cliente/Pessoa`
- Em `Fornecedor`, o telefone é uma string única de 10 ou 11 dígitos (`63999990001`), validada por `Validadores.telefone` e formatada pelo pipe `| formato: 'telefone'`.
- Em `Cliente`, cada telefone é um objeto `{ codigoArea, numero, whatsapp }`, onde `codigoArea` tem 2 dígitos e `numero` tem 8 ou 9 dígitos, validado por regex inline no `FormGroup` (`cliente-form.ts:124-128`) e formatado por `formatarNumeroTelefone`.

---

## 3. Inconsistências entre Recursos Parecidos

1. **`Marca` possui inativação (`ativa`), mas `Fornecedor` e `Categoria` não possuem**:
   - Quando uma `Marca` tem produtos vinculados e não pode ser excluída, o sistema orienta o administrador a inativá-la (`MarcaServiceImpl.java:67`).
   - Quando um `Fornecedor`, `Categoria` ou `Sabor` tem produtos vinculados, a exclusão também é bloqueada (`FornecedorServiceImpl.java:66`, `CategoriaServiceImpl.java:66`, `SaborServiceImpl.java:66`), mas não existe campo `ativo` nessas entidades para inativá-las caso deixem de operar com a loja.
2. **Paginação na listagem de Produtos (`ProdutoList`) vs. Demais listagens Admin**:
   - `ProdutoList` inicia com `pageSize = 5`, opções `[5, 10, 20]` e `showFirstLastButtons` no `<mat-paginator>`.
   - `CategoriaList`, `MarcaList`, `SaborList`, `FornecedorList` e `ClienteList` iniciam com `pageSize = 10`, opções `[5, 10, 20]` e **sem** `showFirstLastButtons`.

---

## 4. Fluxos que se Contradizem ou se Quebram no Meio

1. **Inativação de Produto no Admin vs. Exibição no Catálogo da Loja**:
   - No fluxo administrativo (`/admin/produtos/editar/:id`), o switch `ativo` informa explicitamente ao usuário: *"Produto ativo (visível na loja)"*.
   - Ao desmarcar esse switch e salvar, o produto passa a ter `ativo = false` no banco, porém **continua sendo retornado por `GET /produtos` e exibido no Catálogo da Loja (`/loja`)** (apenas com o botão de compra trocado por `"Indisponível"`), ocupando vagas na paginação de 8 itens por página da vitrine.
2. **Fluxo `"Criar conta"` na Loja (`/loja/login` → `/loja/perfil` → `/loja/login`)**:
   - Na tela `/loja/login`, o usuário sem conta clica em `"Criar conta"` (`routerLink="/loja/perfil"`).
   - Ao chegar em `/loja/perfil` deslogado, a tela exibe apenas a mensagem *"Entre para acompanhar seus pedidos"* com um botão `"Entrar"` que redireciona de volta para `/loja/login`. O fluxo é um loop fechado sem saída para cadastro.
3. **Carregamento de Opções de Relacionamento com Limite Fixo de 100 (`ProdutoForm`, `ProdutoList`, `Catalogo`)**:
   - Os selects de `Categoria`, `Marca`, `Fornecedor` e `Sabor` buscam `listar('', 0, 100)`.
   - Como `Paginacao.TAMANHO_MAXIMO = 100` impede pedir mais de 100 itens e o front-end não pagina nem faz busca assíncrona (autocomplete) nos `<mat-select>`, a partir do 101º registro cadastrado (em ordem alfabética), os registros seguintes ficam inacessíveis para vínculo em `ProdutoForm`.

---

## 5. Dependências Ocultas entre Fluxos

1. **Cadastro de Cliente modifica silenciosamente a tabela de Municípios (`ClienteServiceImpl` → `MunicipioServiceImpl`)**:
   - Uma operação de `POST /clientes` ou `PUT /clientes/{id}` pode inserir um novo registro em `municipio` ou alterar o `codigoIbge` de um `municipio` existente.
   - Se uma requisição de `Cliente` falhar após `municipioService.buscarOuCriar` (por exemplo, erro de constraint ao persistir `Cliente`), o `@Transactional` de `ClienteServiceImpl` faz rollback de ambos; porém, se a operação tiver sucesso com um nome de cidade digitado com erro de grafia (quando sem `codigoIbge`), o município com grafia incorreta passa a existir permanentemente na tabela `municipio`.
2. **Exclusão de Cliente depende de `Pessoa` não ser compartilhada**:
   - `ClienteServiceImpl.delete` apaga fisicamente o registro em `pessoa`. Qualquer evolução futura que vincule `Usuario` ou `Funcionario` à mesma `Pessoa` quebrará o fluxo de `DELETE /clientes/{id}`.
