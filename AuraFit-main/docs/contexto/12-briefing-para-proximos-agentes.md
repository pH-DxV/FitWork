# 12 — Briefing para os Próximos Agentes

> **Instrução de uso:** Copie ou referencie este arquivo no início de qualquer nova tarefa neste repositório. Ele contém tudo o que um agente precisa saber para modificar o **AuraFit** com segurança sem quebrar convenções ou regras existentes.

---

## 1. Visão Rápida da Stack e Estrutura

- **Workspace:** `c:\Users\AVIS 01\Documents\Raphael\AuraFit-main\AuraFit-main` (sem repositório `.git` inicializado).
- **Back-end (`aurafit-main/`):** Quarkus `3.38.3`, Java `21` (`pom.xml`), Hibernate ORM com Panache, PostgreSQL (`localhost:5432/topicos2db`), Hibernate Validator, SmallRye OpenAPI.
  - Pacote base: `br.unitins.topicos1.aurafit` (`model`, `dto`, `mapper`, `repository`, `service`, `resource`, `exception`).
- **Front-end (`aurafit-frontend/`):** Angular `22.1` (100% Standalone Components + Signals + Reactive Forms) + Angular Material 3 (`22.2`), consumindo `http://localhost:8080` (`src/app/api.config.ts`) e ViaCEP (`https://viacep.com.br/ws/{cep}/json/`).

---

## 2. Comandos de Build e Teste (e Armadilhas de Ambiente)

### Back-end (`cd aurafit-main`)
- **Compilar produção + testes:** `mvn test-compile` (usa o Maven global `3.9.16`; **não use `./mvnw.cmd`** em ambiente sem internet pois falta `.mvn/wrapper/maven-wrapper.jar`).
- **Rodar testes unitários (`mvn test`):**
  - **Armadilha conhecida:** `pom.xml` define `<surefire-plugin.version>3.5.6</surefire-plugin.version>` tanto para `maven-surefire-plugin` quanto para `maven-failsafe-plugin`. No cache local `~/.m2`, o `surefire` disponível é `3.5.2` e o `failsafe` é `3.5.6`. Para rodar `mvn test` offline, alinhe a versão do `maven-surefire-plugin` para `3.5.2` no `pom.xml` (mantendo `failsafe` em `3.5.6`).
- **Subir em modo dev:** `mvn quarkus:dev` (⚠️ **Atenção:** `application.properties` está com `drop-and-create`, o que apaga e recria todas as tabelas de `topicos2db` no start!).

### Front-end (`cd aurafit-frontend`)
- **Checagem de tipos (rápida e segura offline):** `npx tsc --noEmit`
- **Build de desenvolvimento (funciona offline):** `npx ng build --configuration development`
- **Build de produção (`npm run build`):** Tenta baixar a fonte `Manrope` de `fonts.googleapis.com` para fazer inlining; em ambiente sem internet, falha a menos que `optimization.fonts.inline: false` seja configurado no `angular.json`.
- **Testes (`npx ng test --watch=false`):** Configurado com Vitest (`@angular/build:unit-test`), mas atualmente não há arquivos `*.spec.ts` no projeto.

---

## 3. Convenções Obrigatórias do Projeto

1. **Entidades (`model`):**
   - Todas estendem [`DefaultEntity`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/DefaultEntity.java) (`id`, `dataCadastro`, `dataAlteracao`).
   - Enums de domínio (`TipoProteina`, `Regiao`) possuem campos `(Long id, String nome)`, método estático `valueOf(Long id)`, `@JsonFormat(shape = JsonFormat.Shape.OBJECT)` e um `AttributeConverter<Enum, Long>` em `model/converterjpa/` anotado com `@Converter(autoApply = true)`.
   - Dados numéricos de documentos/contato (`cpf`, `cnpj`, `cep`, `telefone`, `codigoArea`, `numero`, `codigoIbge`) são salvos **sem máscara** (apenas dígitos).
2. **DTOs e Validação (`dto` & `service`):**
   - Request e Response DTOs são `record` Java.
   - As anotações de Bean Validation (`@NotBlank`, `@Length`, `@CPF`, `@CNPJ`, `@Valid`) ficam nos `*RequestDTO`, e o disparo (`@NotNull @Valid`) fica **nas interfaces `*Service.java`** (não nos `*Resource.java`).
3. **Services e Testes Unitários (`service`):**
   - Campos `@Inject` nos `*ServiceImpl` devem ter visibilidade **package-private** (sem `private`), pois os testes unitários em `src/test/java/br/unitins/topicos1/aurafit/service/` instanciam `new *ServiceImpl()` diretamente e atribuem repositórios fake em memória aos campos.
   - Use sempre `Textos.limparOuNulo(str)` para campos opcionais (converte `""`/`"  "` em `null`) e `.trim()` para campos obrigatórios; converta e-mails para `.toLowerCase()`.
   - Use `Paginacao.validar(page, pageSize)` em qualquer consulta paginada (`TAMANHO_MAXIMO = 100`).
4. **Erros (RFC 7807 — `application/problem+json`):**
   - Para erros de regra de negócio ligados a um campo do formulário, lance `ValidationException.of("nomeDoCampoNoFormAngular", "Mensagem amigável.")`.
   - Para recursos não encontrados por ID/CPF/Sigla na rota, lance `new ResourceNotFoundException("... não encontrado(a).")`.
   - Quando um ID de relacionamento enviado dentro de um `RequestDTO` não existe (ex.: `idCategoria`, `idMarca`, `endereco.uf`), lance `ValidationException.of("idCategoria", "...")` (HTTP 400), e **não** `ResourceNotFoundException` (HTTP 404).
5. **Front-end Angular:**
   - Componentes `standalone`, estado local com `signal()`, injeção com `inject()`, formulários com `ReactiveFormsModule`.
   - Use `aplicarErrosDaApi(this.form, erro)` e `mensagemErro(this.form.get('campo'))` de `src/app/shared/erros-formulario.ts` para integrar automaticamente os erros RFC 7807 da API aos campos do formulário.

---

## 4. Regras de Negócio que Não Podem Ser Quebradas

- **Unicidade:** `Categoria.nome` (case-insensitive), `Sabor.nome` (case-insensitive), `Marca.cnpj`, `Fornecedor.cnpj`, `Pessoa.cpf`, `Pessoa.email` (case-insensitive) e `Municipio.codigoIbge` não podem duplicar.
- **Integridade na Exclusão:** `Categoria`, `Marca`, `Fornecedor` e `Sabor` vinculados a qualquer `Produto` não podem ser excluídos (`ValidationException` no campo `"id"`).
- **Marca Inativa em Produto:** Produto novo ou troca de marca em produto existente não pode apontar para `Marca` com `ativa == false`; porém, editar outros dados de um produto que **já** pertencia àquela marca inativa deve continuar permitido (`ProdutoServiceImpl.java:112-115`).
- **Cliente:** Idade mínima de 16 anos (`IDADE_MINIMA = 16`), ao menos 1 telefone, e resolução automática de `Municipio` por `codigoIbge` ou `(nome, estado.id)` em `MunicipioService.buscarOuCriar`.
