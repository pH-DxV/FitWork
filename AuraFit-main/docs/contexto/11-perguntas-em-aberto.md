# 11 — Perguntas em Aberto (Decisões de Negócio e Intenção)

Conforme a Regra Obrigatória nº 8 da auditoria, as divergências e ambiguidades de negócio encontradas no código não foram decididas unilateralmente. Abaixo estão as perguntas numeradas, com opções de decisão e a recomendação técnica para validação humana.

---

## Q-01: Visibilidade de produtos inativos (`ativo = false`) no Catálogo da Loja (`/loja`)

- **Contexto / Evidência:** No formulário administrativo ([`produto-form.html:107`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/admin/produtos/produto-form/produto-form.html#L107)), o switch diz *"Produto ativo (visível na loja)"*. Porém, o endpoint `GET /produtos` ([`ProdutoRepository.java:22-45`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/repository/ProdutoRepository.java#L22-L45)) retorna todos os produtos (ativos e inativos), e o [`Catalogo`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-frontend/src/app/components/loja/catalogo/catalogo.ts#L106-L108) da loja exibe os produtos inativos na vitrine como `"Indisponível"` (igual a um produto ativo com `estoque = 0`).
- **Opções:**
  1. **(Recomendada)** Filtrar `ativo = true` na vitrine da loja (adicionando query param opcional `ativo` em `GET /produtos`, usado como `ativo=true` pelo `Catalogo` e sem filtro pelo `ProdutoList` do Admin). Assim, produtos inativos ficam ocultos na loja, enquanto produtos ativos com `estoque = 0` continuam visíveis na vitrine como `"Indisponível"` (esgotados).
  2. Manter produtos inativos visíveis na loja como `"Indisponível"` e alterar apenas o rótulo em `produto-form.html` para *"Produto ativo (disponível para venda)"*.
  3. Ocultar da vitrine da loja tanto produtos inativos quanto produtos com `estoque = 0`.

---

## Q-02: Comportamento de exclusão de `Cliente` quando `Pessoa` passar a ser compartilhada com `Usuario`/`Funcionario`

- **Contexto / Evidência:** A documentação ([`Pessoa.java:14-18`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Pessoa.java#L14-L18) e [`diagrama-classes.puml:124`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/docs/diagrama-classes.puml#L124)) prevê que `Usuario` e `Funcionario` usarão `Pessoa` por composição (`@OneToOne`), mas [`ClienteServiceImpl.delete`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/ClienteServiceImpl.java#L92-L98) exclui fisicamente a `Pessoa` junto com o `Cliente`.
- **Opções:**
  1. **(Recomendada)** Ao excluir um `Cliente`, excluir a `Pessoa` somente se ela não possuir vínculo com `Funcionario`/`Usuario` (e bloquear a exclusão do `Cliente` se ele possuir pedidos realizados na loja).
  2. Substituir a exclusão física de `Cliente` por inativação lógica (`cliente.setAtivo(false)`), preservando sempre o registro de `Cliente` e `Pessoa`.
  3. Manter `Pessoa` exclusiva por papel (sem compartilhar a mesma linha de `pessoa` entre `Cliente` e `Funcionario`).

---

## Q-03: Inativação de `Fornecedor`, `Categoria` e `Sabor` com produtos vinculados

- **Contexto / Evidência:** `Marca` possui o campo `ativa: boolean`, permitindo inativá-la quando não pode ser excluída por possuir produtos ([`MarcaServiceImpl.java:65-67`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/MarcaServiceImpl.java#L65-L67)). Já `Fornecedor`, `Categoria` e `Sabor` também bloqueiam exclusão quando possuem produtos, mas **não possuem** campo `ativo`.
- **Opções:**
  1. **(Recomendada)** Manter o modelo atual (apenas `Marca`, `Produto` e `Cliente` possuem flag `ativo`/`ativa`), conforme especificado em [`diagrama-classes.puml`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/docs/diagrama-classes.puml), para não alterar o contrato de banco da disciplina sem solicitação do professor.
  2. Adicionar flag `ativo: boolean` em `Fornecedor` (e opcionalmente em `Categoria`/`Sabor`) com a mesma regra de bloqueio de novos produtos.

---

## Q-04: Unicidade de nome de `Produto`

- **Contexto / Evidência:** `Categoria` e `Sabor` exigem `nome` único no banco e no Service. Já [`Produto`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/model/Produto.java#L17-L18) e [`ProdutoServiceImpl`](file:///c:/Users/AVIS%2001/Documents/Raphael/AuraFit-main/AuraFit-main/aurafit-main/src/main/java/br/unitins/topicos1/aurafit/service/ProdutoServiceImpl.java) permitem cadastrar múltiplos produtos com exatamente o mesmo nome, mesma marca e mesmo peso.
- **Opções:**
  1. **(Recomendada)** Manter sem restrição de unicidade global de `nome` (pois marcas diferentes podem ter produtos com mesmo nome comercial, ex.: `"Creatina 300g"`), ou no máximo validar unicidade composta `(nome, id_marca, pesoGramas)` se desejado.
  2. Tornar `Produto.nome` globalmente único como em `Categoria` e `Sabor`.

---

## Q-05: Escopo da próxima etapa evolutiva no projeto

- **Contexto / Evidência:** Os `README.md` indicam que as entregas 01 a 04 já foram concluídas e que `Login`, `Carrinho`, `Pagamento` e `Perfil` na loja são protótipos aguardando a etapa de `Usuario` + `JWT`.
- **Opções de foco para o próximo ciclo:**
  1. **(Recomendada)** Corrigir primeiro os achados rápidos de consistência da etapa atual (`ACH-04` filtro de ativos na loja, `ACH-06` trim/validação, `ACH-08` alinhamento do `maven-surefire-plugin` para testes rodarem offline, `ACH-10`/`ACH-11` ajustes de formulário) e em seguida iniciar a modelagem de `Usuario`/`Funcionario`/JWT (`ACH-01`, `ACH-03`).
  2. Iniciar diretamente a implementação de autenticação/autorização (`Usuario`/`JWT`) e persistência de pedidos no back-end, sem mexer nos CRUDs atuais.
  3. Focar apenas na criação de testes automatizados (unitários e integração) para os módulos já existentes.
