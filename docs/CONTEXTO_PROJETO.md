# FitWork — Contexto Geral do Projeto

## 1. O que é o FitWork
Plataforma de e-commerce B2B/B2C voltada à venda e gestão de suplementos proteicos (Whey Protein, Albumina, Caseína, Soja e Ervilha), com vitrine pública, carrinho de cotações, portal do cliente e painel administrativo de cadastros, estoque/lotes e imagens.

---

## 2. Arquitetura e Stack Tecnológica

| Camada | Tecnologia | Porta (Dev) | Responsabilidade |
| :--- | :--- | :---: | :--- |
| **Front-end** | Angular 22 + Angular Material + Tailwind CSS | `4200` | Vitrine, login/autocadastro, perfil do cliente e painel administrativo. |
| **Back-end** | Quarkus 3.38 (Java 21) + Hibernate ORM Panache | `8080` | API REST, regras de negócio, autorização, intermediação com o Keycloak e S3. |
| **Banco de Dados** | PostgreSQL | `5432` | Persistência relacional de produtos, categorias, marcas, fornecedores, clientes e endereços. |
| **Identidade (IAM)** | Keycloak 26.4 (OAuth 2.0 / OIDC) | `8180` | Autenticação, emissão de tokens JWT, gestão de senhas e papéis (`ADMIN` e `CLIENTE`). |
| **Object Storage** | RustFS (compatível com Amazon S3) | `9000` / `9001` | Armazenamento de imagens dos produtos no bucket `fitwork-arquivos`. |

### Princípios Arquiteturais
- **Intermediação de Identidade (Backend-for-Frontend):** O Angular **não** se comunica diretamente com o Keycloak e **não** conhece `client_secret` nem credenciais administrativas. Toda operação (`/auth/login`, `/auth/cadastro`, `/auth/refresh`, `/auth/logout`, `/auth/me`) passa pelo Quarkus.
- **Zero Senhas no Banco do E-commerce:** O PostgreSQL armazena apenas o identificador `keycloakId` (`sub`) na tabela `cliente`; credenciais ficam exclusivamente no Keycloak.
- **Refresh Token em Cookie `HttpOnly`:** O `accessToken` (curta duração) fica apenas em memória no Angular e o `refreshToken` trafega somente no cookie `FITWORK_RT` (`HttpOnly`, `SameSite=Strict`).
- **Padronização de Erros (RFC 7807):** Todas as falhas (`400`, `401`, `403`, `404`, `409`, `503`, `500`) retornam `application/problem+json` com `type`, `title`, `status`, `detail`, `instance`, `timestamp` e `errors[]` por campo.

---

## 3. Estrutura do Repositório

```text
FitWork/
├── backend/                     # API Quarkus (Maven, Java 21)
│   └── src/main/java/br/unitins/topicos1/fitwork/
│       ├── dto/                 # Contratos de entrada/saída (Records + Bean Validation)
│       ├── exception/           # RFC 7807 Problem + ExceptionMappers
│       ├── mapper/              # Conversores Entidade <-> DTO
│       ├── model/               # Entidades JPA (DefaultEntity, Produto, Cliente, Pessoa, etc.)
│       ├── repository/          # Repositórios Panache
│       ├── resource/            # Endpoints REST com @PermitAll / @RolesAllowed / @Authenticated
│       └── service/             # Regras de negócio, Auth/Keycloak e Storage S3
├── frontend/                    # Aplicação Angular 22 (standalone components + signals)
│   └── src/app/
│       ├── components/loja/     # Vitrine, carrinho, pagamento, login/cadastro e perfil
│       ├── components/admin/    # Painel e CRUDs administrativos
│       ├── guards/              # authGuard e adminGuard
│       ├── interceptors/        # authInterceptor (Bearer token + tratamento 401/403)
│       ├── models/              # Interfaces TypeScript
│       └── services/            # Comunicação com a API Quarkus e SessaoService
├── keycloak/                    # Realm "fitwork" exportável (fitwork-realm.json)
├── docs/                        # Documentação, regras de negócio e contexto do projeto
├── docker-compose.yml           # Containers RustFS (S3) e Keycloak
└── .env.example                 # Modelo de variáveis de ambiente
```

---

## 4. Visão Geral dos Recursos da API

| Recurso | Caminho Base | Leitura (`GET`) | Escrita (`POST`/`PUT`/`PATCH`/`DELETE`) |
| :--- | :--- | :--- | :--- |
| **Autenticação** | `/auth` | `/me` (`@Authenticated`) | `/login`, `/cadastro`, `/refresh`, `/logout` (`@PermitAll`) |
| **Produtos & Imagens** | `/produtos` | `@PermitAll` | `@RolesAllowed("ADMIN")` |
| **Categorias** | `/categorias` | `@PermitAll` | `@RolesAllowed("ADMIN")` |
| **Marcas** | `/marcas` | `@PermitAll` | `@RolesAllowed("ADMIN")` |
| **Sabores** | `/sabores` | `@PermitAll` | `@RolesAllowed("ADMIN")` |
| **Estados / Municípios / Regiões / Tipos Proteína** | `/estados`, `/municipios`, `/regioes`, `/tipos-proteina` | `@PermitAll` | Somente leitura |
| **Fornecedores** | `/fornecedores` | `@RolesAllowed("ADMIN")` | `@RolesAllowed("ADMIN")` |
| **Clientes** | `/clientes` | `ADMIN` (todos/CPF) ou titular (`/{id}`, `/me`) | `ADMIN` (criar/excluir) ou titular/`ADMIN` (`PUT /{id}`) |
