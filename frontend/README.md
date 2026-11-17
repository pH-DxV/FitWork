# FitWork — Front-end (Angular)

Front-end do projeto **FitWork** (loja de suplementos) da disciplina Tópicos 2.
Feito em **Angular 22 + Angular Material**, consumindo a API Quarkus em `http://localhost:8080`.

## Como rodar

```bash
npm install
npm start          # ou: ng serve
```

Acesse <http://localhost:4200>. O back-end (`../backend`) precisa estar rodando (`./mvnw quarkus:dev`).

## Estrutura (padrão trabalhado em sala)

```
src/app/
├── api.config.ts            # URL do back-end
├── app.routes.ts            # rotas (loja/* e admin/*)
├── models/                  # interfaces (Produto, Marca, Cliente, Pagina<T>, ProblemaApi...)
├── services/                # chamadas HTTP (um service por recurso) + CEP (ViaCEP) + carrinho
├── shared/                  # validadores, máscaras, tratamento de erros da API, diálogo, paginador pt-BR
└── components/
    ├── admin/               # área administrativa
    │   ├── admin-layout/    # menu lateral
    │   ├── painel/          # indicadores
    │   ├── categorias/      # CRUD (lista + formulário)
    │   ├── marcas/          # CRUD
    │   ├── sabores/         # CRUD
    │   ├── fornecedores/    # CRUD
    │   ├── produtos/        # many-to-one + filtros + paginação
    │   └── clientes/        # cadastro de pessoa com ViaCEP + Município/Estado
    └── loja/                # protótipo do e-commerce
        ├── catalogo/        # consulta de produtos (dados reais da API)
        ├── carrinho/
        ├── pagamento/
        ├── login/
        └── perfil/
```

## Onde está cada entrega

| Trabalho | Onde |
|---|---|
| 01 – Protótipo de telas | `/admin/*` (cadastros) e `/loja`, `/loja/login`, `/loja/carrinho`, `/loja/pagamento`, `/loja/perfil` |
| 02 – 2 CRUDs | Marcas (`/admin/marcas`) e Categorias (`/admin/categorias`) — também Sabores e Fornecedores |
| 03 – Tela many-to-one com filtro e paginação | Produtos (`/admin/produtos`): filtros por nome, tipo de proteína, categoria e marca; paginação no back-end |
| 04 – ViaCEP | Clientes (`/admin/clientes/novo`): CEP → ViaCEP; endereço salvo com Município e Estado; validação no front e no back |

> Login, carrinho, pagamento e perfil são **protótipos** (sem autenticação real). A autenticação com Usuário/JWT fica para a próxima etapa.
