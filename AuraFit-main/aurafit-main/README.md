# AuraFit — Back-end (Quarkus)

API REST da loja de suplementos **AuraFit** (disciplina Tópicos 2 — UNITINS).
Estrutura baseada no projeto de referência da disciplina (sga-tp2-2026).

**Tecnologias:** Quarkus 3.38 · Java 25 · Hibernate ORM com Panache · PostgreSQL · Hibernate Validator · SmallRye OpenAPI.

## Como rodar

1. Banco PostgreSQL local:
   - banco `topicos2db`, usuário `topicos2`, senha `123456` (porta 5432)
2. Subir a API em modo dev:

```bash
./mvnw quarkus:dev        # Windows: mvnw.cmd quarkus:dev
```

- API: <http://localhost:8080> (em dev redireciona para o Swagger UI)
- Swagger UI: <http://localhost:8080/q/swagger-ui>
- As tabelas são recriadas a cada start (`drop-and-create`) e populadas pelo `import.sql`.

Testes unitários: `./mvnw test`

## Estrutura de pacotes

```
br.unitins.topicos1.aurafit
├── dto/          records Request/Response (validação com Bean Validation) + PageResponse<T>
├── exception/    erros no formato RFC 7807 (application/problem+json) + ExceptionMappers
├── mapper/       conversão Entidade -> ResponseDTO
├── model/        entidades JPA (todas estendem DefaultEntity) e enums com id/nome
│   └── converterjpa/  AttributeConverter dos enums (salva o id)
├── repository/   PanacheRepository com consultas paginadas (PanacheQuery)
├── resource/     endpoints REST
└── service/      interfaces + implementações (regras de negócio)
```

## Endpoints

| Recurso | Endpoints |
|---|---|
| Categorias, Marcas, Fornecedores, Sabores, Clientes | `GET /x?page=&pageSize=` · `GET /x/nome/{nome}` · `GET /x/{id}` · `POST /x` · `PUT /x/{id}` · `DELETE /x/{id}` |
| Produtos | os mesmos + filtros em `GET /produtos?nome=&idTipoProteina=&idCategoria=&idMarca=&page=&pageSize=` |
| Clientes | + `GET /clientes/cpf/{cpf}` |
| Estados / Municípios | `GET /estados`, `/estados/{id}`, `/estados/sigla/{sigla}`, `/municipios`, `/municipios/nome/{nome}` |
| Enums | `GET /tipos-proteina`, `GET /regioes` |

Listagens devolvem `{ items, page, pageSize, totalItems, totalPages }`.
Erros devolvem `{ type, title, status, detail, instance, timestamp, errors: [{ field, message }] }`.

## Regras de negócio principais

- CNPJ (marca/fornecedor), CPF e e-mail (pessoa) e nome (categoria/sabor) não se repetem.
- Categoria, marca, fornecedor e sabor em uso por produtos não podem ser excluídos.
- Produto novo não pode usar marca inativa.
- Cliente: CPF válido (dígitos verificadores), idade mínima de 16 anos, ao menos um telefone.
- Endereço do cliente: o Município é localizado pelo código IBGE (ou nome + UF) vindo do ViaCEP; se não existir, é cadastrado automaticamente no Estado correspondente.
