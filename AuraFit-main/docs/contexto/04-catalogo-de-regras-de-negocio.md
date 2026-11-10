# 04 — Catálogo de Regras de Negócio

Este documento cataloga todas as regras de negócio explícitas, implícitas e de dados encontradas no back-end (`aurafit-main`) e no front-end (`aurafit-frontend`), além das análises de duplicação, contradições e autorização.

---

## 1. Catálogo Estruturado de Regras de Negócio

### 1.1. Categorias e Sabores

```
ID: RN-001
Regra: O nome da categoria é obrigatório, deve ter entre 2 e 100 caracteres e não pode se repetir (independentemente de maiúsculas/minúsculas).
Onde é aplicada:
- Back-end (Bean Validation): CategoriaRequestDTO.java (L8-L9)
- Back-end (Service/Repo): CategoriaServiceImpl.validarDados (L78-L82), CategoriaRepository.findByNomeExceptId (L15-L20)
- Back-end (DDL): Categoria.java (L9)
- Front-end: CategoriaForm (categoria-form.ts L34)
Tipo: explícita (validação/condição) | de dados (constraint UNIQUE)
Evidência:
if (categoriaRepository.findByNomeExceptId(dto.nome(), id) != null) {
    throw ValidationException.of("nome", "Já existe uma categoria cadastrada com esse nome.");
}
Confiança: CONFIRMADA
Observações: Duplicada em 4 pontos (Front-end, DTO, Service e DDL). Em CategoriaForm.salvar (L67) o front não faz .trim() antes de enviar, permitindo que " a " (3 chars) passe em @Length(min=2) e seja salvo como "a" (1 char) após dto.nome().trim() em CategoriaServiceImpl.aplicarDados (L85).
```

```
ID: RN-002
Regra: Uma categoria que possua produtos vinculados não pode ser excluída.
Onde é aplicada: CategoriaServiceImpl.delete (L63-L69), ProdutoRepository.existeComCategoria (L47-L49)
Tipo: explícita (validação/condição) | de dados (FK NOT NULL em produto.id_categoria)
Evidência:
if (produtoRepository.existeComCategoria(id)) {
    throw ValidationException.of("id", "Esta categoria possui produtos cadastrados e não pode ser excluída.");
}
Confiança: CONFIRMADA
Observações: Impede a exclusão mesmo que todos os produtos vinculados estejam inativos (ativo = false).
```

```
ID: RN-003
Regra: O nome do sabor é obrigatório, deve ter entre 2 e 80 caracteres e não pode se repetir (case-insensitive).
Onde é aplicada:
- Back-end (Bean Validation): SaborRequestDTO.java (L8-L9)
- Back-end (Service/Repo): SaborServiceImpl.validarDados (L78-L82), SaborRepository.findByNomeExceptId (L15-L20)
- Back-end (DDL): Sabor.java (L9)
- Front-end: SaborForm (sabor-form.ts L34)
Tipo: explícita (validação/condição) | de dados (constraint UNIQUE)
Evidência:
if (saborRepository.findByNomeExceptId(dto.nome(), id) != null) {
    throw ValidationException.of("nome", "Já existe um sabor cadastrado com esse nome.");
}
Confiança: CONFIRMADA
Observações: Assim como em Categoria (RN-001), SaborForm.salvar (L67) não aplica .trim() antes do envio, permitindo gravar nome de 1 caractere se enviado cercado por espaços (" x ").
```

```
ID: RN-004
Regra: Um sabor vinculado a qualquer produto não pode ser excluído.
Onde é aplicada: SaborServiceImpl.delete (L63-L69), ProdutoRepository.existeComSabor (L59-L65)
Tipo: explícita (validação/condição) | de dados (FK em produto_sabor.id_sabor)
Evidência:
if (produtoRepository.existeComSabor(id)) {
    throw ValidationException.of("id", "Este sabor está vinculado a produtos e não pode ser excluído.");
}
Confiança: CONFIRMADA
Observações: Nenhuma.
```

---

### 1.2. Marcas e Fornecedores

```
ID: RN-005
Regra: O CNPJ da marca é obrigatório, deve conter exatamente 14 dígitos numéricos com dígitos verificadores válidos e deve ser único entre as marcas.
Onde é aplicada:
- Back-end (Bean Validation): MarcaRequestDTO.java (L16-L19: @NotBlank, @Pattern("\\d{14}"), @CNPJ)
- Back-end (Service/Repo): MarcaServiceImpl.validarDados (L79-L83), MarcaRepository.findByCnpjExceptId (L15-L20)
- Back-end (DDL): Marca.java (L13)
- Front-end: MarcaForm (marca-form.ts L38: Validadores.cnpj) e Validadores.cnpj (validadores.ts L30-L51)
Tipo: explícita (validação/condição) | de dados (constraint UNIQUE)
Evidência:
if (marcaRepository.findByCnpjExceptId(dto.cnpj(), id) != null) {
    throw ValidationException.of("cnpj", "Já existe uma marca cadastrada com esse CNPJ.");
}
Confiança: CONFIRMADA
Observações: O mesmo CNPJ pode existir simultaneamente em Marca e em Fornecedor (tabelas independentes), o que faz sentido quando o fabricante também é fornecedor direto.
```

```
ID: RN-006
Regra: Uma marca que possua produtos cadastrados não pode ser excluída, devendo ser inativada caso não seja mais utilizada.
Onde é aplicada: MarcaServiceImpl.delete (L63-L70), ProdutoRepository.existeComMarca (L51-L53)
Tipo: explícita (validação/condição) | de dados (FK NOT NULL em produto.id_marca)
Evidência:
if (produtoRepository.existeComMarca(id)) {
    throw ValidationException.of("id",
            "Esta marca possui produtos cadastrados e não pode ser excluída. Você pode inativá-la.");
}
Confiança: CONFIRMADA
Observações: A UI de MarcaList (marca-list.ts L81) antecipa essa regra no texto do diálogo de confirmação de exclusão.
```

```
ID: RN-007
Regra: O CNPJ do fornecedor é obrigatório, deve conter 14 dígitos numéricos válidos (@CNPJ) e não pode se repetir entre fornecedores; o telefone do fornecedor é obrigatório e deve ter 10 ou 11 dígitos numéricos (DDD + número).
Onde é aplicada:
- Back-end (Bean Validation): FornecedorRequestDTO.java (L15-L22)
- Back-end (Service/Repo): FornecedorServiceImpl.validarDados (L78-L82), FornecedorRepository.findByCnpjExceptId (L15-L20)
- Back-end (DDL): Fornecedor.java (L13, L17)
- Front-end: FornecedorForm (fornecedor-form.ts L36-L37) e Validadores.cnpj / Validadores.telefone (validadores.ts L30-L59)
Tipo: explícita (validação/condição) | de dados (constraint UNIQUE)
Evidência:
if (fornecedorRepository.findByCnpjExceptId(dto.cnpj(), id) != null) {
    throw ValidationException.of("cnpj", "Já existe um fornecedor cadastrado com esse CNPJ.");
}
Confiança: CONFIRMADA
Observações: Diferente de Marca, Fornecedor não possui campo "ativo/ativa".
```

```
ID: RN-008
Regra: Um fornecedor que possua produtos vinculados não pode ser excluído.
Onde é aplicada: FornecedorServiceImpl.delete (L63-L69), ProdutoRepository.existeComFornecedor (L55-L57)
Tipo: explícita (validação/condição) | de dados (FK NOT NULL em produto.id_fornecedor)
Evidência:
if (produtoRepository.existeComFornecedor(id)) {
    throw ValidationException.of("id", "Este fornecedor possui produtos cadastrados e não pode ser excluído.");
}
Confiança: CONFIRMADA
Observações: Como Fornecedor não tem flag ativo, um fornecedor com produtos vinculados não pode ser nem excluído nem inativado.
```

---

### 1.3. Produtos

```
ID: RN-009
Regra: Um produto novo não pode ser cadastrado com uma marca inativa, e um produto existente não pode ter sua marca alterada para uma marca inativa; porém, um produto que já pertencia a uma marca inativa pode ser editado mantendo essa mesma marca.
Onde é aplicada: ProdutoServiceImpl.aplicarDados (L111-L115)
Tipo: explícita (validação/condição)
Evidência:
Marca marca = buscarMarca(dto.idMarca());
boolean trocouDeMarca = produto.getMarca() == null || !produto.getMarca().getId().equals(marca.getId());
if (trocouDeMarca && !marca.isAtiva()) {
    throw ValidationException.of("idMarca", "Esta marca está inativa e não pode receber novos produtos.");
}
Confiança: CONFIRMADA
Observações: No front-end (produto-form.html L79), o select exibe marcas inativas com o sufixo "(inativa)" mas NÃO as desabilita nem mesmo ao criar um novo produto, deixando o erro acontecer no back-end.
```

```
ID: RN-010
Regra: Todo produto deve ter nome (2..120 chars), preço positivo (>= 0.01, até 8 dígitos inteiros e 2 decimais), peso em gramas positivo (>= 1), estoque não-negativo (>= 0), e vínculos válidos existentes com TipoProteina, Categoria, Marca e Fornecedor; os sabores são opcionais, mas se informados seus IDs devem existir.
Onde é aplicada:
- Back-end (Bean Validation): ProdutoRequestDTO.java (L16-L51)
- Back-end (Service): ProdutoServiceImpl.aplicarDados e métodos buscarTipoProteina, buscarCategoria, buscarMarca, buscarFornecedor, buscarSabores (L117-L175)
- Front-end: ProdutoForm (produto-form.ts L57-L71)
Tipo: explícita (validação/condição)
Evidência:
@DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
@Digits(integer = 8, fraction = 2, message = "Use no máximo 2 casas decimais no preço.")
Confiança: CONFIRMADA
Observações: Divergência no front-end: ProdutoForm (produto-form.ts L62) não valida @Digits(integer=8, fraction=2) para preco, e produto-form.html (L36) define min="0" no input HTML em vez de min="0.01". Além disso, não há constraint de unicidade para Produto.nome (dois produtos podem ter o mesmo nome).
```

```
ID: RN-011
Regra: Os sabores de um produto são sempre devolvidos nas respostas da API em ordem alfabética pelo nome do sabor.
Onde é aplicada: ProdutoMapper.toResponse (L27-L30)
Tipo: implícita (ordenação na transformação de saída)
Evidência:
produto.getSabores().stream()
        .sorted(Comparator.comparing(Sabor::getNome))
        .map(SaborMapper::toResponse)
        .toList()
Confiança: CONFIRMADA
Observações: Testada explicitamente em ProdutoServiceTest.deveCadastrarProdutoResolvendoRelacionamentos (L114).
```

---

### 1.4. Clientes, Pessoas, Telefones, Endereços e Municípios

```
ID: RN-012
Regra: Para ser cadastrado como cliente, a pessoa deve ter data de nascimento no passado e idade mínima de 16 anos completos na data atual.
Onde é aplicada:
- Back-end (Bean Validation): ClienteRequestDTO.java (L32-L34: @NotNull, @Past)
- Back-end (Service): ClienteServiceImpl.validarDados (L29, L108-L111)
- Front-end: ClienteForm (cliente-form.ts L25, L70) e Validadores.dataNascimento (validadores.ts L68-L88)
Tipo: explícita (validação/condição)
Evidência:
if (Period.between(dto.dataNascimento(), LocalDate.now()).getYears() < IDADE_MINIMA) {
    throw ValidationException.of("dataNascimento",
            "O cliente deve ter pelo menos " + IDADE_MINIMA + " anos.");
}
Confiança: CONFIRMADA
Observações: Duplicada e consistente entre back-end e front-end (IDADE_MINIMA = 16).
```

```
ID: RN-013
Regra: O CPF e o e-mail da pessoa são obrigatórios e únicos no sistema (CPF com 11 dígitos válidos pelo módulo 11; e-mail comparado de forma case-insensitive e armazenado em minúsculas).
Onde é aplicada:
- Back-end (Bean Validation): ClienteRequestDTO.java (L22-L30: @Email, @Pattern("\\d{11}"), @CPF)
- Back-end (Service/Repo): ClienteServiceImpl.validarDados (L112-L117), ClienteServiceImpl.aplicarDados (L123), PessoaRepository.findByCpfExceptId (L14-L19), PessoaRepository.findByEmailExceptId (L21-L26)
- Back-end (DDL): Pessoa.java (L25, L28: unique = true)
- Front-end: ClienteForm (cliente-form.ts L68-L69) e Validadores.cpf (validadores.ts L12-L28)
Tipo: explícita (validação/condição) | de dados (constraint UNIQUE)
Evidência:
if (pessoaRepository.findByCpfExceptId(dto.cpf(), idPessoa) != null) {
    throw ValidationException.of("cpf", "Já existe uma pessoa cadastrada com esse CPF.");
}
if (pessoaRepository.findByEmailExceptId(dto.email(), idPessoa) != null) {
    throw ValidationException.of("email", "Já existe uma pessoa cadastrada com esse e-mail.");
}
Confiança: CONFIRMADA
Observações: A validação de unicidade ocorre na tabela pessoa (e não apenas em cliente).
```

```
ID: RN-014
Regra: Todo cliente deve possuir ao menos um telefone válido (DDD de 2 dígitos e número de 8 ou 9 dígitos) e exatamente um endereço completo (CEP de 8 dígitos, logradouro, número, bairro, cidade e UF).
Onde é aplicada:
- Back-end (Bean Validation): ClienteRequestDTO.java (L39-L44: @NotEmpty telefones, @NotNull endereco), TelefoneRequestDTO.java (L8-L18), EnderecoRequestDTO.java (L13-L41)
- Front-end: ClienteForm (cliente-form.ts L72-L82, L122-L139: removerTelefone impede remover quando resta apenas 1 telefone)
Tipo: explícita (validação/condição)
Evidência:
@NotEmpty(message = "Ao menos um telefone deve ser informado.")
List<@Valid TelefoneRequestDTO> telefones,
@NotNull(message = "O endereço deve ser informado.")
@Valid EnderecoRequestDTO endereco
Confiança: CONFIRMADA
Observações: Na atualização de cliente (Pessoa.setTelefones, L88-L97), todos os telefones anteriores são removidos (clear() com orphanRemoval=true) e recriados.
```

```
ID: RN-015
Regra: Ao salvar o endereço de um cliente, o Município é localizado prioritariamente pelo código IBGE (7 dígitos) ou, na ausência/não localização deste, pelo par (nome da cidade case-insensitive + UF). Se o município já existir mas pertencer a uma UF diferente da informada, a operação é rejeitada. Se o município já existir sem código IBGE e o código IBGE for informado, o código IBGE do município é atualizado. Se o município não existir no banco, ele é cadastrado automaticamente vinculado ao Estado correspondente à UF.
Onde é aplicada: MunicipioServiceImpl.buscarOuCriar (L47-L78), chamado por ClienteServiceImpl.aplicarEndereco (L143)
Tipo: explícita (validação/condição) + implícita (auto-cadastro e enriquecimento colateral de dados)
Evidência:
if (municipio != null) {
    if (!municipio.getEstado().getId().equals(estado.getId())) {
        throw ValidationException.of("endereco.uf", "A UF não corresponde ao município informado.");
    }
    if (municipio.getCodigoIbge() == null && codigoIbge != null && !codigoIbge.isBlank()) {
        municipio.setCodigoIbge(codigoIbge);
    }
    return municipio;
}
Confiança: CONFIRMADA
Observações: Regra crítica com efeito colateral de escrita na tabela municipio durante o cadastro/edição de cliente. Se dois municípios homônimos tiverem o mesmo código IBGE enviado erroneamente, ou se o usuário editar manualmente a cidade sem código IBGE, um novo registro é criado em municipio.
```

```
ID: RN-016
Regra: Ao excluir um cliente, a entidade Pessoa associada (junto com seus Telefones e Endereço em cascata) também é excluída fisicamente do banco de dados.
Onde é aplicada: ClienteServiceImpl.delete (L92-L98), Pessoa.java (L37-L42: CascadeType.ALL, orphanRemoval = true em telefones e endereco)
Tipo: implícita (efeito colateral de exclusão)
Evidência:
Cliente cliente = buscarClienteOuFalhar(id);
Pessoa pessoa = cliente.getPessoa();
clienteRepository.delete(cliente);
// a pessoa (com telefones e endereço) pertence apenas ao cliente
pessoaRepository.delete(pessoa);
Confiança: CONFIRMADA
Observações: Conflita com o plano arquitetural documentado em Pessoa.java (L16) e diagrama-classes.puml (L124) de compartilhar a mesma Pessoa com Usuario/Funcionario no futuro.
```

---

### 1.5. Regras Transversais (Paginação, Normalização e Protótipo da Loja)

```
ID: RN-017
Regra: Todas as consultas paginadas exigem índice de página >= 0 e tamanho de página entre 1 e 100.
Onde é aplicada: Paginacao.validar (Paginacao.java L12-L19), chamada em todos os *ServiceImpl.paginar
Tipo: explícita (validação/condição)
Evidência:
if (page < 0) {
    throw ValidationException.of("page", "A página não pode ser negativa.");
}
if (pageSize < 1 || pageSize > TAMANHO_MAXIMO) {
    throw ValidationException.of("pageSize", "O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO + ".");
}
Confiança: CONFIRMADA
Observações: Os selects de ProdutoForm, ProdutoList e Catalogo usam pageSize = 100 (o limite máximo exato).
```

```
ID: RN-018
Regra: Campos textuais obrigatórios sofrem trim() antes de persistir; campos textuais opcionais vazios ou compostos apenas por espaços são convertidos para null; e-mails (Pessoa, Marca, Fornecedor) são sempre convertidos para letras minúsculas (toLowerCase()).
Onde é aplicada: Textos.limparOuNulo (Textos.java L9-L11), CategoriaServiceImpl (L85-L86), MarcaServiceImpl (L86-L89), FornecedorServiceImpl (L85-L89), SaborServiceImpl (L85-L86), ProdutoServiceImpl (L117-L118), ClienteServiceImpl (L122-L126, L139-L142)
Tipo: implícita (normalização silenciosa)
Evidência:
public static String limparOuNulo(String texto) {
    return (texto == null || texto.isBlank()) ? null : texto.trim();
}
Confiança: CONFIRMADA
Observações: Garante que strings vazias "" nunca sejam salvas em colunas opcionais (descricao, emailContato, email, genero, complemento).
```

```
ID: RN-019
Regra: Na loja virtual (protótipo), um produto só pode ser adicionado ao carrinho se estiver ativo e com estoque maior que zero; a quantidade de um mesmo item (produto + sabor) no carrinho é limitada ao estoque disponível do produto; o frete custa R$ 19,90 e torna-se grátis para subtotais a partir de R$ 199,00.
Onde é aplicada:
- Catalogo.disponivel (catalogo.ts L106-L108)
- CarrinhoService (carrinho.service.ts L10-L11, L25, L42-L43)
Tipo: explícita (no front-end)
Evidência:
export const VALOR_FRETE = 19.9;
export const FRETE_GRATIS_A_PARTIR = 199;
...
frete = computed(() => (this.subtotal() === 0 || this.subtotal() >= FRETE_GRATIS_A_PARTIR ? 0 : VALOR_FRETE));
Confiança: CONFIRMADA
Observações: Regra 100% restrita ao front-end (protótipo). Em alterarQuantidade (carrinho.service.ts L42), usa `const limite = Math.max(item.produto.estoque, 1)`, o que permitiria manter 1 unidade caso um produto com estoque 0 já estivesse no localStorage.
```

---

## 2. Regras Implícitas e Nomes que Prometem Mais do que Fazem

1. **"Produto ativo (visível na loja)" (`produto-form.html:107`) vs. Exibição real no Catálogo (`catalogo.ts:67-86`)**:
   - O rótulo do switch no formulário administrativo afirma que `ativo` controla se o produto é *"visível na loja"*.
   - **Comportamento real:** `ProdutoRepository.buscar` não filtra `ativo = true`, e `Catalogo` exibe os produtos inativos na vitrine da loja com o botão desabilitado `"Indisponível"` (mesmo comportamento visual de um produto ativo com `estoque = 0`).
2. **"Pix (5% de desconto)" (`pagamento.html:67`) vs. Cálculo real (`pagamento.ts:108-113`)**:
   - O rótulo na tela de checkout promete 5% de desconto no Pix, mas `confirmar()` registra `this.carrinho.total()` sem aplicar qualquer desconto.
3. **Travamento de Cidade/UF no `ClienteForm` quando há `codigoIbge` (`cliente-form.ts:178-180, 293-297`)**:
   - Quando um CEP é resolvido pelo ViaCEP (ou ao editar um cliente cujo município já possui `codigoIbge`), os campos `cidade` e `uf` ficam desabilitados (`disable()`) até que o usuário clique explicitamente em `"Editar cidade/UF"`, momento em que `codigoIbge` é zerado (`''`) para evitar que o back-end associe o código IBGE antigo a outra cidade digitada manualmente.
4. **Ordenação implícita em todos os `findAll()` dos Repositórios**:
   - Todos os 9 repositórios sobrescrevem `findAll()` do Panache para aplicar `order by nome` (ou `order by razaoSocial` em `FornecedorRepository` e `order by pessoa.nome` em `ClienteRepository`).

---

## 3. Regras Duplicadas ou Contraditórias

| Regra | Onde está duplicada | Estado de Consistência |
|---|---|---|
| Validação de CPF (11 dígitos + dígitos verificadores) | `ClienteRequestDTO` (`@Pattern`, `@CPF`) vs. `Validadores.cpf` (`validadores.ts`) | **Consistente** (ambos validam formato de 11 dígitos, rejeitam sequências repetidas e calculam os 2 dígitos módulo 11). |
| Validação de CNPJ (14 dígitos + dígitos verificadores) | `MarcaRequestDTO` / `FornecedorRequestDTO` (`@Pattern`, `@CNPJ`) vs. `Validadores.cnpj` (`validadores.ts`) | **Consistente** (ambos validam 14 dígitos, sequências repetidas e pesos módulo 11). |
| Idade mínima de 16 anos para Cliente | `ClienteServiceImpl` (`IDADE_MINIMA = 16`) vs. `ClienteForm` (`IDADE_MINIMA = 16` + `Validadores.dataNascimento`) | **Consistente**. |
| Tamanho mínimo de nomes (`min = 2`) vs. `.trim()` | Front-end (`Validators.minLength(2)`) vs. Back-end (`@Length(min = 2)` no DTO + `.trim()` no Service) | **Inconsistente nas bordas**:<br>1. Em `CategoriaForm` e `SaborForm`, o front não faz `.trim()` antes de enviar; `" a "` (3 chars) passa no front e no `@Length(min=2)` do DTO, e o Service salva `"a"` (1 char) após `.trim()`.<br>2. Em `MarcaForm`, `FornecedorForm`, `ProdutoForm` e `ClienteForm`, o front valida `minLength(2)` antes do `.trim()`, mas envia `.trim()` no payload; digitar `"a "` passa no form do Angular e falha com HTTP 400 no back-end. |
| Validação de preço do Produto | `ProdutoRequestDTO` (`@DecimalMin("0.01")`, `@Digits(integer=8, fraction=2)`) vs. `ProdutoForm` (`Validators.min(0.01)`, HTML `min="0"`) | **Parcialmente divergente**: o front-end não valida `@Digits(integer=8, fraction=2)` e tem `min="0"` no atributo HTML do input. |

---

## 4. Autorização como Regra de Negócio

- **Estado atual confirmado:** **Não existe nenhuma regra de autenticação ou autorização implementada no servidor nem no front-end administrativo.**
  - No back-end (`aurafit-main`): não há dependência `quarkus-security` ou `quarkus-smallrye-jwt`, nem anotações `@RolesAllowed` / `@Authenticated` em nenhum dos 11 Resources. Todos os endpoints (`POST`, `PUT`, `DELETE`, `GET /clientes`) são públicos para qualquer cliente HTTP.
  - No front-end (`aurafit-frontend`): as rotas `/admin/*` não possuem `canActivate`. Na loja (`/loja/pagamento` e `/loja/carrinho`), há apenas uma verificação de protótipo em memória (`if (!this.sessao.usuario()) this.router.navigate(['/loja/login'], ...)`), onde `sessao.entrar(email)` aceita qualquer e-mail e qualquer senha de `>= 6` caracteres sem consultar o back-end.
