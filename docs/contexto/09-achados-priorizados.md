# 09 — Achados Priorizados e O Que Deve Ser Preservado

Este documento lista os achados da auditoria em ordem de severidade e prioridade, seguidos pelo registro explícito das decisões de engenharia bem executadas que devem ser preservadas nas próximas etapas.

---

## 1. Lista Priorizada de Achados

### ACH-01
```
[SEVERIDADE: Crítico] [CONFIRMADO] Ausência de autenticação e autorização na API REST e nas rotas administrativas
Onde:
- Back-end: todos os 11 arquivos em aurafit-main/src/main/java/br/unitins/topicos1/aurafit/resource/*.java e pom.xml (L37-L95)
- Front-end: aurafit-frontend/src/app/app.routes.ts (L40-L70)
Comportamento atual: Nenhuma rota REST do back-end possui verificação de identidade, token JWT ou perfil de acesso (@RolesAllowed / @Authenticated), e as rotas /admin/* do front-end não possuem guardas (canActivate). Qualquer cliente HTTP pode ler dados pessoais sensíveis (CPF, data de nascimento, endereço, telefones em GET /clientes) ou excluir/alterar registros.
Intenção aparente: Segundo aurafit-frontend/README.md (L51) e aurafit-main/docs/diagrama-classes.puml (L124), a autenticação com Usuario/JWT e Funcionario está planejada como a próxima etapa evolutiva da disciplina.
Impacto: Exposição total de dados pessoais (PII) e permissão irrestrita de escrita/exclusão para qualquer usuário anônimo.
Como confirmar (se hipótese): Confirmado por inspeção direta de pom.xml, resources e app.routes.ts.
Direção sugerida: Implementar as entidades Usuario/Funcionario (compondo Pessoa), endpoints de autenticação JWT (quarkus-smallrye-jwt), proteção @RolesAllowed nos Resources e AuthGuard/HttpInterceptor no Angular.
Esforço estimado: G
```

### ACH-02
```
[SEVERIDADE: Alto] [CONFIRMADO] Configuração drop-and-create ativa sem restrição de perfil e credenciais de banco em texto claro
Onde:
- aurafit-main/src/main/resources/application.properties (L3-L7, L12-L13)
- aurafit-main/README.md (L11)
Comportamento atual: A propriedade quarkus.hibernate-orm.database.generation=drop-and-create está definida sem o prefixo %dev., recriando o schema do zero a cada inicialização em qualquer perfil. Além disso, usuário e senha do PostgreSQL estão em texto claro nas linhas 3 e 4, e o log de parâmetros SQL (log.bind-parameters=true) está ativo globalmente (L13).
Intenção aparente: Facilitar a execução local em sala de aula com dados iniciais de import.sql.
Impacto: Risco de perda total de dados caso a aplicação seja iniciada apontando para um banco persistente fora de desenvolvimento, exposição de credenciais no repositório e gravação de CPF/dados pessoais em logs de servidor.
Como confirmar (se hipótese): Confirmado por leitura de application.properties (L3-L13).
Direção sugerida: Restringir drop-and-create e logs SQL ao perfil %dev. (ex.: %dev.quarkus.hibernate-orm.database.generation=drop-and-create) e externalizar credenciais via variáveis de ambiente (${DB_USER:topicos2}, ${DB_PASSWORD}).
Esforço estimado: P
```

### ACH-03
```
[SEVERIDADE: Alto] [CONFIRMADO] Exclusão física incondicional de Pessoa ao excluir Cliente conflita com o compartilhamento planejado de Pessoa
Onde:
- aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/ClienteServiceImpl.java (L92-L98)
- aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Pessoa.java (L14-L18)
- aurafit-main/docs/diagrama-classes.puml (L124)
Comportamento atual: ClienteServiceImpl.delete(long id) executa clienteRepository.delete(cliente) e, logo na linha 97, executa pessoaRepository.delete(pessoa) assumindo que "a pessoa pertence apenas ao cliente".
Intenção aparente: O Javadoc de Pessoa.java (L15-L17) e a nota em diagrama-classes.puml (L124) estabelecem que Usuario e Funcionario reutilizarão a mesma entidade Pessoa por composição (@OneToOne).
Impacto: Assim que uma mesma Pessoa for vinculada simultaneamente a Cliente e a Usuario/Funcionario, excluir o Cliente causará erro de integridade referencial (FK violation → HTTP 500) ou excluirá os dados pessoais de um funcionário/usuário ativo.
Como confirmar (se hipótese): Confirmado por leitura de ClienteServiceImpl.java (L92-L98).
Direção sugerida: Antes de deletar Pessoa, verificar se ela não está vinculada a outras entidades (Usuario/Funcionario) ou adotar inativação lógica (cliente.setAtivo(false)).
Esforço estimado: P
```

### ACH-04
```
[SEVERIDADE: Alto] [CONFIRMADO] Produtos inativos continuam sendo retornados e exibidos na vitrine da loja virtual
Onde:
- aurafit-frontend/src/app/components/admin/produtos/produto-form/produto-form.html (L107)
- aurafit-frontend/src/app/components/loja/catalogo/catalogo.ts (L67-L86, L106-L108)
- aurafit-main/src/main/java/br/unitins/topicos1/aurafit/repository/ProdutoRepository.java (L22-L45)
- aurafit-main/src/main/java/br/unitins/topicos1/aurafit/resource/ProdutoResource.java (L34-L41)
Comportamento atual: O endpoint GET /produtos não possui parâmetro para filtrar por ativo, retornando produtos ativos e inativos. O componente Catalogo da loja consome GET /produtos e renderiza produtos com ativo = false na grade da vitrine como "Indisponível" (por exemplo, o produto 7 do import.sql: "Whey Isolado Zero Lactose 450g", que tem ativo = false).
Intenção aparente: No formulário de produto (produto-form.html:107), o rótulo do toggle afirma explicitamente: "Produto ativo (visível na loja)". Produtos inativos não deveriam aparecer para o consumidor final na vitrine (diferente de produtos ativos com estoque = 0, que aparecem como esgotados).
Impacto: Produtos descontinuados/inativados pelo administrador continuam visíveis na loja e consomem slots da paginação do catálogo.
Como confirmar (se hipótese): Confirmado lendo ProdutoRepository.buscar (L22-L45), catalogo.ts (L67-L86) e import.sql (L81).
Direção sugerida: Adicionar filtro opcional `@QueryParam("ativo") Boolean ativo` em `GET /produtos` (e em `ProdutoRepository.buscar`), passando `ativo: true` nas chamadas originadas pelo `Catalogo` da loja.
Esforço estimado: P
```

### ACH-05
```
[SEVERIDADE: Médio] [CONFIRMADO] Limite rígido de 100 itens no carregamento de selects trunca relacionamentos a partir do 101º registro
Onde:
- aurafit-frontend/src/app/components/admin/produtos/produto-form/produto-form.ts (L78-L81)
- aurafit-frontend/src/app/components/admin/produtos/produto-list/produto-list.ts (L64-L65)
- aurafit-frontend/src/app/components/loja/catalogo/catalogo.ts (L64)
- aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/Paginacao.java (L7, L16-L18)
Comportamento atual: Para popular os <mat-select> de Categoria, Marca, Fornecedor e Sabor, o front-end requisita apenas a página 0 com pageSize = 100 (limite máximo permitido por Paginacao.TAMANHO_MAXIMO).
Intenção aparente: Carregar todas as opções disponíveis para seleção nos formulários e filtros.
Impacto: Caso o banco ultrapasse 100 registros em qualquer uma dessas tabelas, os itens a partir do 101º jamais aparecerão nos selects. Se um produto em edição estiver vinculado a um item fora dos 100 primeiros, o campo select ficará visualmente vazio.
Como confirmar (se hipótese): Confirmado por leitura de produto-form.ts (L78-L81) e Paginacao.java (L7).
Direção sugerida: Criar endpoints leves de listagem para seleção (ou autocomplete paginado com busca por nome no select) quando o volume puder superar 100 registros.
Esforço estimado: M
```

### ACH-06
```
[SEVERIDADE: Médio] [CONFIRMADO] Validação @Length(min = 2) executada antes do .trim() permite gravar nomes de 1 caractere
Onde:
- aurafit-main/src/main/java/br/unitins/topicos1/aurafit/dto/CategoriaRequestDTO.java (L9), SaborRequestDTO.java (L9), MarcaRequestDTO.java (L13), FornecedorRequestDTO.java (L12), ProdutoRequestDTO.java (L17), ClienteRequestDTO.java (L19)
- aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/CategoriaServiceImpl.java (L85), SaborServiceImpl.java (L85)
- aurafit-frontend/src/app/components/admin/categorias/categoria-form/categoria-form.ts (L34, L67)
- aurafit-frontend/src/app/components/admin/sabores/sabor-form/sabor-form.ts (L34, L67)
Comportamento atual:
1. Em CategoriaForm e SaborForm, o front-end envia o valor sem .trim(). Se o usuário digitar " a " (3 caracteres), passa no front-end, passa no @Length(min = 2) do DTO no back-end e sofre .trim() no Service, persistindo "a" (1 caractere) no banco.
2. Em MarcaForm, FornecedorForm, ProdutoForm e ClienteForm, o front-end valida minLength(2) antes do .trim(), mas aplica .trim() ao montar o payload: digitar "a " passa na validação do formulário Angular, mas falha com erro 400 na API.
Intenção aparente: Garantir que todo nome útil (após remoção de espaços nas extremidades) tenha no mínimo 2 caracteres.
Impacto: Gravação de registros com 1 caractere em Categoria/Sabor e feedback tardio de validação nos demais formulários.
Como confirmar (se hipótese): Confirmado por leitura dos DTOs, Services e componentes *-form.ts.
Direção sugerida: No front-end, validar o comprimento útil em `Validadores` (ou fazer trim em `CategoriaForm`/`SaborForm`); no back-end, validar o tamanho após `trim()` ou usar regex/validador que exija ao menos 2 caracteres não-brancos.
Esforço estimado: P
```

### ACH-07
```
[SEVERIDADE: Médio] [CONFIRMADO] Consultas N+1 nas listagens paginadas de Produtos e Clientes
Onde:
- aurafit-main/src/main/java/br/unitins/topicos1/aurafit/repository/ProdutoRepository.java (L15-L45, L68-L70)
- aurafit-main/src/main/java/br/unitins/topicos1/aurafit/mapper/ProdutoMapper.java (L24-L30)
- aurafit-main/src/main/java/br/unitins/topicos1/aurafit/repository/ClienteRepository.java (L15-L22)
- aurafit-main/src/main/java/br/unitins/topicos1/aurafit/mapper/ClienteMapper.java (L13-L22)
Comportamento atual: Produto.sabores (@ManyToMany LAZY), Cliente.pessoa (@OneToOne LAZY) e Pessoa.telefones (@OneToMany LAZY) são carregados sob demanda dentro do loop de mapeamento (query.page(...).list().stream().map(Mapper::toResponse)), disparando múltiplas queries adicionais por página. Além disso, ProdutoServiceImpl.buscarSabores (L167-L173) executa um SELECT por sabor em loop ao salvar produto.
Intenção aparente: Retornar os DTOs completos com suas coleções e associações aninhadas.
Impacto: Degradação de performance proporcional ao pageSize (até 100 itens por página -> mais de 100 queries por requisição GET).
Como confirmar (se hipótese): Confirmado pelas anotações JPA nas entidades, queries Panache sem JOIN FETCH e quarkus.hibernate-orm.log.sql=true.
Direção sugerida: Configurar `@BatchSize` nas coleções/entidades associadas (`Produto.sabores`, `Pessoa.telefones`, `Pessoa`) ou usar `JOIN FETCH` nas consultas de leitura, e buscar sabores em lote (`find("id in ?1", ids)`) em `ProdutoServiceImpl.buscarSabores`.
Esforço estimado: P
```

### ACH-08
```
[SEVERIDADE: Médio] [CONFIRMADO] Execução offline de testes e builds falha no Maven Wrapper, no Surefire e no inline de fontes do Angular
Onde:
- aurafit-main/pom.xml (L22, L114, L125)
- aurafit-main/.mvn/wrapper/maven-wrapper.properties (L1-L4)
- aurafit-frontend/src/index.html (L10-L14) e aurafit-frontend/angular.json (L30-L45)
Comportamento atual:
1. No back-end, pom.xml usa ${surefire-plugin.version} = 3.5.6 tanto para maven-surefire-plugin quanto para maven-failsafe-plugin, mas o cache local possui surefire 3.5.2 e failsafe 3.5.6, impedindo `mvn test` sem internet. Além disso, `.mvn/wrapper/maven-wrapper.jar` não está presente, quebrando `mvnw.cmd` offline.
2. No front-end, `ng build` (produção) falha sem internet ao tentar baixar a fonte Manrope de `fonts.googleapis.com`, e `ng test` falha com erro porque não existe nenhum arquivo `*.spec.ts`.
Intenção aparente: Permitir que `./mvnw test`, `npm run build` e `npm test` funcionem conforme documentado nos READMEs.
Impacto: Impossibilidade de rodar `mvn test` e `npm run build` em ambientes sem acesso externo à internet, e ausência total de testes automatizados no front-end.
Como confirmar (se hipótese): Confirmado executando os comandos no terminal durante a Fase 0.
Direção sugerida: Sincronizar a versão do `maven-surefire-plugin` no cache/pom, incluir `maven-wrapper.jar` (ou usar `mvn`), configurar `"optimization": { "fonts": { "inline": false } }` (ou self-host da fonte) se o build precisar rodar offline, e adicionar ao menos um teste inicial `*.spec.ts` no front-end.
Esforço estimado: P
```

### ACH-09
```
[SEVERIDADE: Médio] [CONFIRMADO] Lacunas de cobertura nos testes do Back-end (Bean Validation, Fornecedor, Sabor e Cliente.delete não testados)
Onde:
- aurafit-main/src/test/java/br/unitins/topicos1/aurafit/service/*Test.java
Comportamento atual: Existem apenas 4 classes de teste unitário puro que instanciam `*ServiceImpl` com `new`. Como não há container CDI nos testes, nenhuma anotação de Bean Validation (`@Valid`, `@CPF`, `@CNPJ`, `@Email`, `@NotBlank`, `@Digits`) é testada. Além disso, `FornecedorServiceImpl`, `SaborServiceImpl`, `EstadoServiceImpl`, `ClienteServiceImpl.delete`, `ClienteServiceImpl.findByCpf`, a validação de e-mail duplicado em `ClienteServiceImpl` e os `ExceptionMappers` têm 0% de testes.
Intenção aparente: Cobrir as regras de negócio dos serviços principais com testes unitários rápidos em memória.
Impacto: Regressões em anotações de Bean Validation, nos CRUDs de Fornecedor/Sabor ou no mapeamento RFC 7807 não são detectadas pelos testes.
Como confirmar (se hipótese): Confirmado pela leitura integral dos 5 arquivos em `src/test/`.
Direção sugerida: Adicionar testes unitários para `FornecedorServiceImpl`, `SaborServiceImpl`, e-mail duplicado e exclusão em `ClienteServiceTest`, além de testes com `Validator` do Jakarta Validation (ou `@QuarkusTest`).
Esforço estimado: M
```

### ACH-10
```
[SEVERIDADE: Baixo] [CONFIRMADO] Consulta ViaCEP para CEP geral de município sobrescreve logradouro e bairro já digitados em ClienteForm
Onde:
- aurafit-frontend/src/app/components/admin/clientes/cliente-form/cliente-form.ts (L169-L176)
Comportamento atual: Em `ClienteForm.buscarCep()`, `this.endereco.patchValue` atribui diretamente `logradouro: resposta.logradouro` e `bairro: resposta.bairro`. Para municípios com CEP único/geral (onde o ViaCEP retorna `logradouro: ""` e `bairro: ""`), qualquer texto que o usuário já tivesse digitado nesses dois campos é apagado (diferente de `complemento` na L175, que preserva o valor existente com `this.endereco.controls.complemento.value || resposta.complemento`).
Intenção aparente: Preencher automaticamente os dados vindos do ViaCEP sem perder dados já informados pelo usuário quando o ViaCEP retorna vazio.
Impacto: Perda de dados digitados no formulário ao reconsultar CEPs gerais de municípios.
Como confirmar (se hipótese): Confirmado por inspeção das linhas 169–176 de `cliente-form.ts`.
Direção sugerida: Usar `resposta.logradouro || this.endereco.controls.logradouro.value` e `resposta.bairro || this.endereco.controls.bairro.value` quando o campo no ViaCEP vier vazio.
Esforço estimado: P
```

### ACH-11
```
[SEVERIDADE: Baixo] [CONFIRMADO] Divergências de UX e validação no Front-end (preço sem validação de casas decimais, seleção de marca inativa, typo e protótipo da loja)
Onde:
- aurafit-frontend/src/app/components/admin/produtos/produto-form/produto-form.ts (L62) e produto-form.html (L36, L79)
- aurafit-frontend/src/app/components/admin/sabores/sabor-form/sabor-form.html (L4)
- aurafit-frontend/src/app/components/loja/pagamento/pagamento.html (L67) e pagamento.ts (L108)
- aurafit-frontend/src/app/components/loja/login/login.html (L37) e perfil/perfil.html (L50)
- aurafit-frontend/src/app/components/loja/loja-layout/loja-layout.html (L12) e catalogo/catalogo.html (L38)
Comportamento atual:
1. `ProdutoForm` não valida máximo de 2 casas decimais nem 8 dígitos inteiros em `preco` (exigidos por `@Digits(integer=8, fraction=2)` no back-end), tem `min="0"` no HTML (enquanto o validador exige `0.01`), e permite selecionar marcas inativas em novos produtos (que sempre falharão no back-end).
2. `sabor-form.html:4` contém o erro de digitação `"Voltar paro sabores"`.
3. No protótipo da loja: `"Pix (5% de desconto)"` não calcula desconto; o link `"Criar conta"` no login leva para `/loja/perfil` que devolve para `/loja/login`; e clicar em `"limpar busca"` no catálogo não limpa o texto do `<input>` no cabeçalho (`LojaLayout`).
Intenção aparente: Fornecer feedback imediato no front-end espelhando o back-end e navegação fluida no protótipo.
Impacto: Pequenos atritos de usabilidade e inconsistências visuais.
Como confirmar (se hipótese): Confirmado por leitura direta dos templates e componentes listados.
Direção sugerida: Adicionar `Validators.pattern(/^\d{1,8}(\.\d{1,2})?$/)` e `min="0.01"` em `preco`, desabilitar marcas inativas não vinculadas no `<mat-option>` de `ProdutoForm`, corrigir o typo em `sabor-form.html` e ajustar os detalhes do protótipo da loja.
Esforço estimado: P
```

---

## 2. O Que Está Bem Feito e Deve Ser Preservado

A auditoria constatou qualidades arquiteturais e de implementação acima da média que **devem ser mantidas** por qualquer agente ou desenvolvedor que atuar neste projeto:

1. **Contrato de Erros Padronizado (RFC 7807) e Integração Automática com Angular Forms:**
   - A dupla [`ConstraintViolationExceptionMapper.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/exception/ConstraintViolationExceptionMapper.java) / [`ValidationExceptionMapper.java`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/exception/ValidationExceptionMapper.java) no back-end e [`erros-formulario.ts`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/shared/erros-formulario.ts) no front-end é exemplar. O mapeamento automático de caminhos aninhados (`"endereco.uf"`, `"telefones.0.numero"`) diretamente para `form.get(field)` — inclusive redirecionando erros de controles `disabled` para o alerta geral — funciona de forma limpa e não deve ser quebrado.
2. **Separação Clara de Camadas e Simetria Estrutural:**
   - Tanto o back-end (`resource` → `service` → `repository` → `model`, com `dto` e `mapper` puros) quanto o front-end (`components` → `services` → `models` + `shared`) seguem convenções previsíveis e uniformes em todos os módulos.
3. **Modelagem de `TipoProteina` e `Regiao` com `AttributeConverter`:**
   - Persistir os enums pelo seu `id` explícito via `TipoProteinaConverter` e `RegiaoConverter` (em vez de `EnumType.ORDINAL` ou `STRING`) evita corrupção silenciosa caso a ordem dos enums mude no código.
4. **Regra Inteligente de Edição de Produto com Marca Inativa (`ProdutoServiceImpl.java:112-115`):**
   - A verificação `trocouDeMarca && !marca.isAtiva()` permite editar preço/estoque de um produto antigo cuja marca foi inativada, enquanto bloqueia o uso da marca inativa em novos produtos ou trocas.
5. **Tratamento Cuidadoso do ViaCEP no `ClienteForm` (`cliente-form.ts:143-195`):**
   - O bloqueio automático de `cidade` e `uf` quando o CEP retorna `codigoIbge` do ViaCEP, aliado ao botão explícito `"Editar cidade/UF"` que limpa o `codigoIbge` ao destravar os campos, evita inconsistências entre o código IBGE e municípios digitados manualmente.
6. **Normalização Centralizada de Textos (`Textos.limparOuNulo` e `Textos.somenteDigitos`):**
   - Evita colunas opcionais preenchidas com `""` ou `"   "` no banco de dados e padroniza CPF, CNPJ, CEP e telefones apenas com dígitos no armazenamento.
