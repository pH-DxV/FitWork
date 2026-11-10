# 05 — Fichas de Fluxos de Ponta a Ponta

Este documento detalha cada fluxo de negócio principal de ponta a ponta (Front-end Angular → REST Resource → CDI Service → Panache Repository → Banco PostgreSQL → Mapper → Resposta HTTP), incluindo limites transacionais e referências ao Catálogo de Regras de Negócio (`RN-001` a `RN-019`).

---

## Ficha 01 — CRUD de Domínios Simples: Categorias e Sabores

### 1. Entrada
- **Rotas de UI:** `/admin/categorias`, `/admin/categorias/nova`, `/admin/categorias/editar/:id` e `/admin/sabores`, `/admin/sabores/novo`, `/admin/sabores/editar/:id`.
- **Endpoints REST:**
  - `GET /categorias?page=0&pageSize=10` e `GET /categorias/nome/{nome}?page=0&pageSize=10`
  - `GET /categorias/{id}`
  - `POST /categorias` e `PUT /categorias/{id}` — Body JSON: `CategoriaRequestDTO { nome: String, descricao: String }`
  - `DELETE /categorias/{id}`
  - *(Idêntico para `/sabores` com `SaborRequestDTO`)*

### 2. Caminho Completo e Limites Transacionais
1. **Front-end:** [`CategoriaForm.salvar()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/admin/categorias/categoria-form/categoria-form.ts#L59-L82) valida o `FormGroup` e chama [`CategoriaService.incluir()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/services/categoria.service.ts#L26) ou `alterar()`.
2. **Resource:** [`CategoriaResource.incluir()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/resource/CategoriaResource.java#L49-L52) / `alterar()` / `apagar()` delega diretamente para `CategoriaService`.
3. **Interceptor de Validação CDI:** Na entrada de [`CategoriaService.create/update`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/CategoriaService.java#L17-L19), o Hibernate Validator valida `@NotNull @Valid CategoriaRequestDTO`. Se falhar, lança `ConstraintViolationException` **antes** de abrir a transação.
4. **Limite Transacional (`@Transactional`):** Inicia ao entrar em [`CategoriaServiceImpl.create`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/CategoriaServiceImpl.java#L43) / `update` (L53) / `delete` (L62). *(Nota: `findAll`, `findByNome` e `findById` em `CategoriaServiceImpl` e `SaborServiceImpl` **não** possuem `@Transactional`, pois `Categoria` e `Sabor` não têm relacionamentos `LAZY`).*
5. **Service & Repository:**
   - Em `create`/`update`: chama `validarDados(dto, id)` → [`CategoriaRepository.findByNomeExceptId`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/repository/CategoriaRepository.java#L15-L20); aplica `aplicarDados` (`trim` e `Textos.limparOuNulo`); persiste no Panache.
   - Em `delete`: busca com `buscarCategoriaOuFalhar(id)`, verifica [`ProdutoRepository.existeComCategoria(id)`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/repository/ProdutoRepository.java#L47-L49) e chama `categoriaRepository.delete(categoria)`.
6. **Mapper:** [`CategoriaMapper.toResponse`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/mapper/CategoriaMapper.java#L11-L13) converte para `CategoriaResponseDTO`.

### 3. Regras Aplicadas
- `RN-001` (unicidade e tamanho de nome de Categoria: 2..100 chars) / `RN-003` (Sabor: 2..80 chars).
- `RN-002` (bloqueio de exclusão de Categoria com produtos) / `RN-004` (bloqueio de exclusão de Sabor com produtos).
- `RN-017` (validação de paginação `0 <= page`, `1 <= pageSize <= 100`).
- `RN-018` (normalização `trim()` e string vazia → `null` em `descricao`).

### 4. Efeitos
- **Leitura:** `SELECT` em `categoria` / `sabor` (e `SELECT count(*)` em `produto` ou `produto_sabor` no `DELETE`).
- **Gravação:** `INSERT`, `UPDATE` ou `DELETE` em `categoria` / `sabor`, com atualização automática de `dataCadastro` (`@PrePersist`) ou `dataAlteracao` (`@PreUpdate`).

### 5. Saída
- `GET` lista: `200 OK` com `PageResponse<CategoriaResponseDTO>`.
- `GET /{id}`: `200 OK` ou `404 Not Found` (`application/problem+json`).
- `POST`: `201 Created` com `CategoriaResponseDTO` ou `400 Bad Request` (`errors: [{ field, message }]`).
- `PUT /{id}`: `200 OK` ou `400`/`404`.
- `DELETE /{id}`: `204 No Content` ou `400` (possui produtos) / `404`.

### 6. Comportamento Observado (Evidência em Testes)
- Comprovado por leitura e pelos 3 testes unitários em [`CategoriaServiceTest.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/test/java/br/unitins/topicos1/aurafit/service/CategoriaServiceTest.java#L34-L56) (`deveCadastrarCategoriaComDescricaoVaziaComoNula`, `deveRejeitarNomeDuplicado`, `naoDeveExcluirCategoriaComProdutos`). `SaborServiceImpl` não possui testes unitários.

### 7. Divergências
- `CategoriaForm` e `SaborForm` enviam `this.form.getRawValue()` sem `.trim()`, permitindo que `" a "` passe na validação `@Length(min = 2)` e seja gravado como `"a"` (1 caractere) após o `.trim()` no Service.

---

## Ficha 02 — CRUD de Marcas e Fornecedores

### 1. Entrada
- **Rotas de UI:** `/admin/marcas`, `/admin/marcas/nova`, `/admin/marcas/editar/:id` e `/admin/fornecedores`, `/admin/fornecedores/novo`, `/admin/fornecedores/editar/:id`.
- **Endpoints REST:**
  - `GET /marcas`, `GET /marcas/nome/{nome}`, `GET /marcas/{id}`, `POST /marcas`, `PUT /marcas/{id}`, `DELETE /marcas/{id}` — Body: `MarcaRequestDTO { nome, cnpj, emailContato, ativa }`.
  - `GET /fornecedores`, `GET /fornecedores/nome/{nome}`, `GET /fornecedores/{id}`, `POST /fornecedores`, `PUT /fornecedores/{id}`, `DELETE /fornecedores/{id}` — Body: `FornecedorRequestDTO { razaoSocial, cnpj, telefone, email }`.

### 2. Caminho Completo e Limites Transacionais
1. **Front-end:** [`MarcaForm.salvar()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/admin/marcas/marca-form/marca-form.ts#L79-L106) e [`FornecedorForm.salvar()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/admin/fornecedores/fornecedor-form/fornecedor-form.ts#L84-L112) removem máscaras de CNPJ/telefone (`somenteNumeros`) e aplicam `.trim()` nos textos antes de enviar à API.
2. **Resource → Service:** `MarcaResource` / `FornecedorResource` delegam para `MarcaService` / `FornecedorService`.
3. **Limite Transacional (`@Transactional`):** Presente em `create`, `update` e `delete` de [`MarcaServiceImpl`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/MarcaServiceImpl.java#L43-L70) e [`FornecedorServiceImpl`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/FornecedorServiceImpl.java#L43-L69).
4. **Validação e Persistência:** Verifica unicidade de CNPJ (`findByCnpjExceptId`), normaliza e-mail para `toLowerCase()`, persiste ou valida ausência de produtos antes de excluir (`existeComMarca` / `existeComFornecedor`).

### 3. Regras Aplicadas
- `RN-005` e `RN-006` (Marca: CNPJ único e válido, bloqueio de exclusão com produtos orientando inativação).
- `RN-007` e `RN-008` (Fornecedor: CNPJ único e válido, telefone 10..11 dígitos, bloqueio de exclusão com produtos).
- `RN-017` e `RN-018` (paginação e normalização de e-mail para minúsculas).

### 4. Efeitos
- **Leitura/Gravação:** Tabelas `marca` e `fornecedor` (e consulta de contagem em `produto` na exclusão).

### 5. Saída
- `200 OK`, `201 Created`, `204 No Content`, `400 Bad Request` (`application/problem+json`), `404 Not Found`.

### 6. Comportamento Observado (Evidência em Testes)
- Comprovado pelos 7 testes unitários em [`MarcaServiceTest.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/test/java/br/unitins/topicos1/aurafit/service/MarcaServiceTest.java#L39-L95). `FornecedorServiceImpl` não possui testes unitários.

### 7. Divergências
- `GET /fornecedores/nome/{nome}` usa a rota `/nome/{nome}` e o método `findByNome`, mas no banco filtra pelo campo `razaoSocial` (intencional para manter simetria de URL com os demais recursos).

---

## Ficha 03 — Catálogo, Busca Multifiltro e CRUD de Produtos

### 1. Entrada
- **Rotas de UI:** `/admin/produtos`, `/admin/produtos/novo`, `/admin/produtos/editar/:id` e `/loja` (Catálogo).
- **Endpoints REST:**
  - `GET /produtos?nome=&idTipoProteina=&idCategoria=&idMarca=&page=0&pageSize=10`
  - `GET /produtos/nome/{nome}?page=0&pageSize=10`
  - `GET /produtos/{id}`
  - `POST /produtos` e `PUT /produtos/{id}` — Body: `ProdutoRequestDTO { nome, descricao, preco, pesoGramas, estoque, idTipoProteina, ativo, idCategoria, idMarca, idFornecedor, idsSabores }`
  - `DELETE /produtos/{id}`

### 2. Caminho Completo e Limites Transacionais
1. **Front-end (Listagem/Catálogo):** [`ProdutoList.carregar()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/admin/produtos/produto-list/produto-list.ts#L69-L91) (com `pageSize = 5`) e [`Catalogo.carregar()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/catalogo/catalogo.ts#L67-L86) (com `pageSize = 8`) chamam [`ProdutoService.listar()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/services/produto.service.ts#L16-L24).
2. **Front-end (Formulário):** [`ProdutoForm.ngOnInit()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/admin/produtos/produto-form/produto-form.ts#L75-L91) dispara `forkJoin` para `/tipos-proteina`, `/categorias?pageSize=100`, `/marcas?pageSize=100`, `/fornecedores?pageSize=100` e `/sabores?pageSize=100`.
3. **Resource:** [`ProdutoResource`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/resource/ProdutoResource.java) delega para `ProdutoService`.
4. **Limite Transacional (`@Transactional`):** Em [`ProdutoServiceImpl`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/ProdutoServiceImpl.java#L48-L100), **todos os 7 métodos públicos** (`findAll`, `findByNome`, `buscar`, `findById`, `create`, `update`, `delete`) são anotados com `@Transactional` porque `Produto.sabores` é uma coleção `@ManyToMany` `LAZY` que precisa da sessão Hibernate aberta durante a execução de `ProdutoMapper.toResponse`.
5. **Service & Repository:**
   - Em `buscar`: converte `idTipoProteina` via `TipoProteina.valueOf` (lançando `ValidationException` se inválido) e monta query dinâmica em [`ProdutoRepository.buscar`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/repository/ProdutoRepository.java#L22-L45).
   - Em `create`/`update`: executa `aplicarDados` (L110-L128), validando se a `Marca` existe e se `trocouDeMarca && !marca.isAtiva()`, resolvendo `TipoProteina`, `Categoria`, `Fornecedor` e cada `Sabor` de `idsSabores` individualmente (`saborRepository.findById(id)` em loop).
6. **Mapper:** [`ProdutoMapper.toResponse`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/mapper/ProdutoMapper.java#L14-L31) converte a entidade e ordena `sabores` alfabeticamente por `nome`.

### 3. Regras Aplicadas
- `RN-009` (restrição de marca inativa para novos produtos ou troca de marca).
- `RN-010` (preço, peso, estoque e existência de relacionamentos).
- `RN-011` (ordenação alfabética dos sabores no retorno).
- `RN-017` e `RN-018`.

### 4. Efeitos
- **Leitura:** Consultas em `produto`, `categoria`, `marca`, `fornecedor`, `sabor` e `produto_sabor`.
- **Gravação:** `INSERT`/`UPDATE`/`DELETE` em `produto` e recriação de vínculos em `produto_sabor`.

### 5. Saída
- `200 OK` (`PageResponse<ProdutoResponseDTO>` ou `ProdutoResponseDTO`), `201 Created`, `204 No Content`, `400 Bad Request`, `404 Not Found`.

### 6. Comportamento Observado (Evidência em Testes)
- Comprovado pelos 5 testes unitários em [`ProdutoServiceTest.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/test/java/br/unitins/topicos1/aurafit/service/ProdutoServiceTest.java#L104-L143).

### 7. Divergências
1. **Visibilidade de produtos inativos na loja:** `produto-form.html:107` diz *"Produto ativo (visível na loja)"*, mas `GET /produtos` não possui filtro de `ativo`, e `Catalogo` exibe produtos inativos na vitrine como `"Indisponível"`.
2. **Código morto:** `ProdutoServiceImpl.findAll(int, int)` nunca é chamado por `ProdutoResource` (que usa `service.buscar(...)`).

---

## Ficha 04 — Cadastro e Edição de Cliente com Integração ViaCEP e Auto-Cadastro de Município

### 1. Entrada
- **Rotas de UI:** `/admin/clientes`, `/admin/clientes/novo`, `/admin/clientes/editar/:id`.
- **Endpoints REST e Externos:**
  - Externo (Front-end → ViaCEP): `GET https://viacep.com.br/ws/{cep}/json/`
  - Auxiliar (Front-end → Back-end): `GET /estados?page=0&pageSize=30`
  - CRUD (Front-end → Back-end): `GET /clientes`, `GET /clientes/nome/{nome}`, `GET /clientes/{id}`, `GET /clientes/cpf/{cpf}`, `POST /clientes`, `PUT /clientes/{id}`, `DELETE /clientes/{id}` — Body: `ClienteRequestDTO`.

### 2. Caminho Completo e Limites Transacionais
1. **Consulta ViaCEP no Front-end:**
   - Em [`ClienteForm.buscarCep()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/admin/clientes/cliente-form/cliente-form.ts#L143-L188), ao sair do campo CEP ou clicar em buscar, [`CepService.consultar(cep)`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/services/cep.service.ts#L12-L16) faz requisição HTTPS ao ViaCEP.
   - Se `resposta.erro` for verdadeiro, retorna `null` e libera edição manual de `cidade` e `uf`.
   - Se encontrar, preenche `logradouro`, `bairro`, `cidade`, `uf`, `codigoIbge` e `complemento`, e **desabilita** os controles `cidade` e `uf` (`disable()`).
2. **Submissão do Formulário:**
   - [`ClienteForm.toRequest()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/admin/clientes/cliente-form/cliente-form.ts#L238-L265) usa `this.form.getRawValue()` (que inclui os campos desabilitados `cidade` e `uf`), limpa máscaras (`somenteNumeros` em `cpf`, `cep`, `codigoArea`, `numero`) e envia `POST /clientes` ou `PUT /clientes/{id}`.
3. **Resource & Validação CDI:**
   - [`ClienteResource`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/resource/ClienteResource.java) chama `ClienteService.create/update`. O interceptor CDI valida recursivamente `ClienteRequestDTO`, `List<@Valid TelefoneRequestDTO>` e `@Valid EnderecoRequestDTO`.
4. **Limite Transacional (`@Transactional`):**
   - Todos os 7 métodos públicos de [`ClienteServiceImpl`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/ClienteServiceImpl.java#L41-L98) são `@Transactional` (necessário inclusive nas leituras porque `Cliente.pessoa` é `@OneToOne(fetch = FetchType.LAZY)` e `Pessoa.telefones` é `@OneToMany` `LAZY`).
   - Dentro da mesma transação de `create`/`update`, `ClienteServiceImpl.aplicarEndereco` chama [`MunicipioServiceImpl.buscarOuCriar`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/MunicipioServiceImpl.java#L47-L78) (também `@Transactional`, participando da transação ativa).
5. **Resolução/Criação de Município e Persistência em Cascata:**
   - `buscarOuCriar` busca `Estado` por `uf` (`EstadoRepository.findBySigla`), tenta localizar `Municipio` por `codigoIbge` e depois por `(nome, estado.id)`. Valida se a UF do município encontrado bate com a UF informada; atualiza `codigoIbge` se estava nulo; ou persiste um novo `Municipio`.
   - `ClienteServiceImpl` persiste `Cliente`, que em cascata (`PERSIST`/`MERGE` para `Pessoa` e `ALL` de `Pessoa` para `Telefone` e `Endereco`) grava todas as entidades.
   - No `DELETE /clientes/{id}`, `ClienteServiceImpl.delete` remove `Cliente` e em seguida remove explicitamente `Pessoa` (que remove `Telefone` e `Endereco` em cascata).

### 3. Regras Aplicadas
- `RN-012` (idade mínima de 16 anos).
- `RN-013` (unicidade de CPF e e-mail em `Pessoa`).
- `RN-014` (ao menos 1 telefone e 1 endereço obrigatório).
- `RN-015` (localização/auto-cadastro de `Municipio` via código IBGE ou nome+UF).
- `RN-016` (exclusão de `Pessoa` junto com `Cliente`).
- `RN-017` e `RN-018`.

### 4. Efeitos
- **Leitura:** `pessoa`, `cliente`, `estado`, `municipio`, `endereco`, `telefone`.
- **Gravação:** `INSERT`/`UPDATE` potencial em `municipio`; `INSERT`/`UPDATE`/`DELETE` em `endereco`, `pessoa`, `telefone` e `cliente`.

### 5. Saída
- `200 OK` / `201 Created` com `ClienteResponseDTO` achatando os dados de `Cliente` e `Pessoa`.
- `204 No Content` no `DELETE`.
- `400 Bad Request` / `404 Not Found` em formato `application/problem+json`.

### 6. Comportamento Observado (Evidência em Testes)
- Comprovado pelos 6 testes unitários em [`ClienteServiceTest.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/test/java/br/unitins/topicos1/aurafit/service/ClienteServiceTest.java#L106-L165) (incluindo criação automática de município quando não existe, rejeição de menor de 16 anos, CPF duplicado, UF inexistente e substituição de telefones no update).

### 7. Divergências
- Em CEPs gerais de cidade onde o ViaCEP retorna `logradouro: ""` e `bairro: ""`, `ClienteForm.buscarCep()` sobrescreve com `""` qualquer logradouro ou bairro que o usuário já tivesse preenchido antes de reconsultar o CEP.

---

## Ficha 05 — Consultas Auxiliares Somente Leitura (Estados, Municípios, Regiões, Tipos de Proteína)

### 1. Entrada
- `GET /estados?page=0&pageSize=30`, `GET /estados/nome/{nome}?page=0&pageSize=10`, `GET /estados/{id}`, `GET /estados/sigla/{sigla}`
- `GET /municipios?page=0&pageSize=10`, `GET /municipios/nome/{nome}?page=0&pageSize=10`, `GET /municipios/{id}`
- `GET /tipos-proteina`, `GET /tipos-proteina/{id}`
- `GET /regioes`, `GET /regioes/{id}`

### 2. Caminho Completo
- `EstadoResource` → `EstadoServiceImpl` → `EstadoRepository` → `EstadoMapper.toResponse`.
- `MunicipioResource` → `MunicipioServiceImpl` → `MunicipioRepository` → `MunicipioMapper.toResponse`.
- `TipoProteinaResource` / `RegiaoResource` → leitura direta de `TipoProteina.values()` / `valueOf(id)` e `Regiao.values()` / `valueOf(id)` em memória (sem Service e sem banco).

### 3. Regras Aplicadas
- `RN-017` (validação de paginação em `/estados` e `/municipios`).

### 4. Efeitos
- Somente leitura (`SELECT` em `estado` e `municipio`, ou leitura de constantes enum em memória).

### 5. Saída
- `200 OK` (`PageResponse<EstadoResponseDTO>`, `PageResponse<MunicipioResponseDTO>`, `TipoProteina[]`, `Regiao[]`) ou `404 Not Found`.

---

## Ficha 06 — Fluxo de Compra no E-Commerce (Protótipo Front-end: Catálogo → Carrinho → Login → Pagamento → Perfil)

### 1. Entrada
- **Rotas de UI:** `/loja` → `/loja/carrinho` → `/loja/login?retorno=/loja/pagamento` → `/loja/pagamento` → `/loja/perfil`.

### 2. Caminho Completo
1. **Adição ao Carrinho:** Em [`Catalogo.adicionar(produto)`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/catalogo/catalogo.ts#L114-L118), obtém o sabor selecionado (ou o primeiro sabor do produto) e chama [`CarrinhoService.adicionar(produto, sabor, 1)`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/services/carrinho.service.ts#L28-L35), que atualiza o signal `itens` e grava em `localStorage['aurafit.carrinho']`.
2. **Revisão no Carrinho:** [`Carrinho.finalizar()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/carrinho/carrinho.ts#L34-L40) verifica `sessao.usuario()`. Se `null`, redireciona para `/loja/login?retorno=/loja/pagamento`.
3. **Login Simulado:** [`Login.entrar()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/login/login.ts#L36-L44) valida `email` e `senha` (mín. 6 chars), chama [`SessaoService.entrar(email)`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/services/sessao.service.ts#L28-L31) (em memória) e navega para a URL `retorno`.
4. **Checkout / Pagamento:** [`Pagamento.buscarCep()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/pagamento/pagamento.ts#L71-L96) consulta o ViaCEP para preencher o endereço de entrega. [`Pagamento.confirmar()`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/pagamento/pagamento.ts#L98-L116) gera número aleatório `'AF-' + Math.floor(10000 + Math.random() * 89999)`, adiciona ao signal `SessaoService.pedidos`, limpa o `CarrinhoService` e exibe a confirmação na tela.

### 3. Regras Aplicadas
- `RN-019` (disponibilidade para compra, limite de quantidade pelo estoque, cálculo de frete R$ 19,90 e frete grátis >= R$ 199,00).

### 4. Efeitos
- **Nenhum efeito no back-end ou no banco de dados.** Não reduz o estoque do `Produto` no PostgreSQL nem grava pedido na API. Apenas manipula `localStorage` e signals em memória no navegador.

### 5. Divergências
1. O rótulo `"Pix (5% de desconto)"` em [`pagamento.html`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/pagamento/pagamento.html#L67) não aplica desconto no valor total.
2. O link `"Criar conta"` em [`login.html`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/login/login.html#L37) leva para `/loja/perfil`, que exibe apenas um botão de volta para `/loja/login`.
