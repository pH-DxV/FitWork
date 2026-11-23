# FitWork — Regras de Negócio da API

## 1. Regras Transversais
- **Paginação padrão (`Paginacao`):** Todas as listagens paginadas aceitam `page` (base 0) e `pageSize` (padrão `10`), retornando `PageResponse<T>` com metadados de página, total de elementos e total de páginas.
- **Higienização de texto (`Textos`):** Strings recebidas passam por `trim()`; campos opcionais em branco viram `null`; documentos/códigos (CPF, CNPJ, CEP, DDD, telefone) têm caracteres não numéricos removidos antes da persistência ou consulta.
- **Comprimento útil:** Campos textuais obrigatórios validam o tamanho após `trim()` (evitando valores compostos apenas por espaços).
- **Exclusão protegida (`409 Conflict`):** Categorias, Marcas, Sabores e Fornecedores vinculados a pelo menos um Produto não podem ser excluídos enquanto houver vínculo.

---

## 2. Autenticação, Autorização e Sessão (`AuthServiceImpl`, `AuthResource`, `KeycloakHttpGateway`)

### 2.1. Políticas de Acesso (RBAC + Deny-by-Default)
- Endpoints sem anotação de segurança são bloqueados por padrão (`403 Forbidden`).
- Papéis reconhecidos no Realm `fitwork`: `ADMIN` e `CLIENTE`.
- **Proteção contra força bruta (Keycloak):** Bloqueio temporário após 5 tentativas inválidas consecutivas.
- **Mensagem genérica no login:** Credenciais inválidas retornam `401 Unauthorized` sem revelar se o e-mail existe ou se apenas a senha está errada.
- **Bloqueio de cliente inativo:** Se o cliente estiver com `ativo = false` no PostgreSQL, o back-end revoga imediatamente a sessão recém-criada no Keycloak e retorna `403 Forbidden`.

### 2.2. Autocadastro (`POST /auth/cadastro`)
1. **Política de senha (Keycloak + Bean Validation):** Obrigatório ter entre 8 e 64 caracteres, com pelo menos 1 letra maiúscula, 1 minúscula e 1 número.
2. **Validação prévia no banco:** Antes de chamar o Keycloak, o sistema valida idade mínima, CPF e unicidade de CPF/e-mail no PostgreSQL.
3. **Atribuição fixa de papel:** Todo autocadastro recebe exclusivamente o papel `CLIENTE` no Keycloak (nunca `ADMIN`).
4. **Compensação transacional:** Se a criação no Keycloak tiver sucesso, mas a gravação de `Pessoa`/`Cliente` no PostgreSQL falhar, o usuário recém-criado é removido do Keycloak automaticamente para evitar contas órfãs.
5. **Nenhuma senha no banco:** Somente o `keycloakId` (`sub`) é salvo em `Cliente`.

### 2.3. Renovação, Perfil e Logout
- `POST /auth/refresh`: lê apenas o cookie `HttpOnly` `FITWORK_RT`; com rotação de refresh token ativa no Keycloak, cada uso invalida o token anterior.
- `GET /auth/me`: exige token JWT válido (`@Authenticated`), identifica o usuário pelo `sub` e vincula automaticamente um `Cliente` pré-existente pelo e-mail caso `keycloakId` ainda seja nulo.
- `POST /auth/logout`: notifica o endpoint de logout do Keycloak e expira o cookie `FITWORK_RT`.

---

## 3. Produtos e Imagens S3 (`ProdutoServiceImpl`, `S3StorageServiceImpl`)

### 3.1. Cadastro e Alteração de Produto
- **Nome:** obrigatório, entre 2 e 120 caracteres úteis.
- **Tipo de Proteína (`idTipoProteina`):** obrigatório e deve corresponder a um valor válido do enum `TipoProteina` (`1..5`).
- **Relacionamentos obrigatórios:** Categoria, Marca e Fornecedor devem existir no banco.
- **Marca ativa obrigatória:** Não é permitido cadastrar ou alterar um produto vinculando-o a uma Marca com `ativo = false` (`400 Bad Request`).
- **Sabores (`idsSabores`):** Lista não vazia; cada ID informado deve existir no cadastro de Sabores.
- **Preço e Estoque:** Preço positivo (até 8 inteiros e 2 decimais); estoque >= 0; peso em gramas > 0.
- **Exclusão de Produto:** Ao excluir um produto que possua imagem vinculada no S3, o objeto também é removido do bucket.

### 3.2. Armazenamento de Imagens no S3 (`PATCH/GET/DELETE /produtos/{id}/imagem`)
- **Restrição de acesso:** Upload (`PATCH`) e remoção (`DELETE`) exigem `ADMIN`; visualização/download (`GET`) é público (`@PermitAll`).
- **Tamanho máximo:** `5 MB` (e arquivo não vazio).
- **Formatos permitidos:** Apenas `image/jpeg`, `image/png` e `image/webp`.
- **Verificação por Magic Bytes:** O conteúdo binário real do arquivo é inspecionado (assinatura JPEG `FF D8 FF`, PNG `89 50 4E 47` ou RIFF/WEBP) para impedir upload de arquivos disfarçados com extensão/Content-Type falso.
- **Substituição limpa:** Ao enviar uma nova imagem para um produto que já tinha outra, o arquivo antigo é removido do bucket após o novo ser salvo.

---

## 4. Clientes, Pessoas e Endereços (`ClienteServiceImpl`, `ClienteResource`, `MunicipioServiceImpl`)

### 4.1. Regras Cadastrais
- **Nome:** obrigatório, entre 2 e 120 caracteres úteis.
- **Idade mínima:** Cliente deve ter pelo menos **16 anos** completos na data do cadastro/atualização.
- **CPF:** obrigatório, válido (11 dígitos + dígitos verificadores) e **único** na tabela `Pessoa` (`409 Conflict` se duplicado).
- **E-mail:** obrigatório, formato válido e **único** na tabela `Pessoa` (`409 Conflict` se duplicado).
- **Telefones:** Ao menos 1 telefone obrigatório (DDD com 2 dígitos, número com 8 ou 9 dígitos).
- **Endereço e Município automático:** Endereço é obrigatório (CEP com 8 dígitos). Ao salvar, `MunicipioService` localiza o Município pelo `codigoIbge` (ou por nome + UF) e valida que a UF informada corresponde ao município; caso o município ainda não exista na base local, ele é criado automaticamente vinculado ao Estado correspondente.

### 4.2. Segurança e Controle de Titularidade (Anti-IDOR)
- **Listagem geral (`GET /clientes`), busca por CPF (`GET /clientes/por-cpf`), criação avulsa (`POST /clientes`) e exclusão (`DELETE /clientes/{id}`):** restritos a `ADMIN`.
- **Consulta/Alteração por ID (`GET/PUT /clientes/{id}`):** permitidos para `ADMIN` (qualquer ID) ou para `CLIENTE` **somente quando o `id` pertence ao próprio usuário autenticado**. Tentativa de acessar/alterar outro ID retorna `403 Forbidden`.
- **Proteção contra autoescalação:** Quando um `CLIENTE` atualiza o próprio cadastro (`PUT /clientes/{id}`), o campo `ativo` é mantido forçadamente como `true`.
- **Sincronização com Keycloak:** Alterações em nome, e-mail ou status (`ativo`) via `PUT /clientes/{id}` ou exclusão via `DELETE /clientes/{id}` são sincronizadas com a conta vinculada no Keycloak (quando `keycloakId` estiver presente).

---

## 5. Categorias, Marcas, Sabores e Fornecedores

| Entidade | Regras Específicas |
| :--- | :--- |
| **Categoria** | Nome obrigatório (2 a 100 caracteres úteis) e **único** (case-insensitive). Não pode ser excluída se houver produtos vinculados. |
| **Marca** | Nome obrigatório (2 a 100 caracteres úteis), CNPJ válido (14 dígitos) e **único**, campo `ativo` obrigatório. Marca inativa impede vínculo com novos produtos. Não pode ser excluída se houver produtos vinculados. |
| **Sabor** | Nome obrigatório (2 a 80 caracteres úteis) e **único** (case-insensitive). Não pode ser excluído se houver produtos vinculados. |
| **Fornecedor** | Acesso integralmente restrito a `ADMIN`. Razão social obrigatória (2 a 120 caracteres úteis), CNPJ válido (14 dígitos) e **único**, e-mail corporativo e ao menos 1 telefone. Não pode ser excluído se houver produtos vinculados. |
