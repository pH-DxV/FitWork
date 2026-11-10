# 10 — Mapa de Cobertura da Auditoria

Este documento declara de forma transparente e verificável o que foi lido na íntegra, o que não precisou ser lido (artefatos gerados/terceiros) e o que foi efetivamente executado versus apenas analisado estaticamente.

---

## 1. Cobertura de Leitura de Arquivos

| Escopo / Diretório | Total de Arquivos | Lidos Integralmente (100%) | Lidos Parcialmente | Não Lidos | Observações |
|---|---|---|---|---|---|
| **Back-end: Configuração, Docs e SQL** (`pom.xml`, `README.md`, `application.properties`, `import.sql`, `diagrama-classes.puml`, `.gitignore`, `.dockerignore`, `.mvn/wrapper/*`) | 9 | **9 (100%)** | 0 | 0 | `Diagrama_aurafit.png` (binário) tem seu fonte em `diagrama-classes.puml`, que foi lido 100%. Scripts `mvnw`/`mvnw.cmd` verificados. |
| **Back-end: Dockerfiles** (`src/main/docker/*`) | 4 | **4 (100%)** | 0 | 0 | `Dockerfile.jvm`, `legacy-jar`, `native`, `native-micro`. |
| **Back-end: Código-Fonte Java** (`src/main/java/br/unitins/topicos1/aurafit/**`) | 92 | **92 (100%)** | 0 | 0 | Todos os 16 `model`/`converterjpa`, 19 `dto`, 9 `exception`, 10 `mapper`, 9 `repository`, 11 `resource` e 18 `service` foram lidos linha a linha. |
| **Back-end: Testes Unitários** (`src/test/java/br/unitins/topicos1/aurafit/**`) | 5 | **5 (100%)** | 0 | 0 | Todas as 4 classes `*ServiceTest.java` e o helper `Entidades.java` foram lidos na íntegra. |
| **Front-end: Configuração e Raiz** (`package.json`, `angular.json`, `tsconfig*.json`, `README.md`, `.prettierrc`, `.editorconfig`, `.gitignore`, `.vscode/*`) | 10 | **10 (100%)** | 0 | 0 | `package-lock.json` (gerado pelo npm) e `favicon.svg` inventariados. |
| **Front-end: Código-Fonte Angular** (`src/**`: `.ts`, `.html`, `.css`, `.scss`) | 69 | **69 (100%)** | 0 | 0 | Todos os arquivos de bootstrap, `models/` (11), `services/` (10), `shared/` (7), `components/admin/` (33) e `components/loja/` (18) foram lidos integralmente. |
| **Diretórios Gerados / Dependências** (`node_modules/`, `target/`, `.angular/`, `dist/`) | — | Ignorados | — | — | Artefatos de build e pacotes de terceiros (fora do escopo de código autoral). |

**Resumo de Cobertura de Leitura do Código Autoral:** **100% dos arquivos de código-fonte, testes, SQL, documentação e configuração do projeto foram lidos integralmente.**

---

## 2. Cobertura de Execução vs. Análise Estática

Seguindo a Regra Obrigatória nº 2 (segurança na execução e preservação de bancos de dados), abaixo está o que foi executado na prática e o que foi verificado por análise estática:

| Verificação | Modo | Resultado Obtido | Por que não foi além (se aplicável) |
|---|---|---|---|
| **Compilação do Back-end (`mvn test-compile`)** | **Executado** | `BUILD SUCCESS` (92 classes principais + 5 classes de teste compiladas com JDK 21.0.12.1 e Maven 3.9.16). | — |
| **Execução do Maven Wrapper (`mvnw.cmd -v`)** | **Executado** | Falhou (`Cannot start maven from wrapper`) por ausência de `.mvn/wrapper/maven-wrapper.jar` e bloqueio de rede no sandbox. | — |
| **Execução dos Testes JUnit (`mvn test`)** | **Tentado / Analisado Estaticamente** | Falhou na resolução do plugin `maven-surefire-plugin:3.5.6` (o cache `~/.m2` local contém `surefire:3.5.2` e `failsafe:3.5.6`). Os 21 métodos de teste foram auditados estaticamente linha por linha. | Regra 1 proíbe alterar `pom.xml` para trocar a versão do plugin. |
| **Subida da API Quarkus (`mvn quarkus:dev`) e Requisições Reais ao PostgreSQL** | **Apenas Leitura (Não Executado)** | Não executado. | `application.properties` configura `drop-and-create` contra `jdbc:postgresql://localhost:5432/topicos2db`. Pela Regra 2 (*"sem comandos destrutivos, sem rodar migrações ou scripts de carga contra um banco que contenha dados reais"*), subir a aplicação destruiria o banco local `topicos2db` caso existisse. |
| **Checagem de Tipos do Front-end (`npx tsc --noEmit`)** | **Executado** | Sucesso (código de saída `0`, zero erros de compilação TypeScript). | — |
| **Build de Desenvolvimento do Front-end (`npx ng build --configuration development`)** | **Executado** | Sucesso (`Application bundle generation complete`, `main.js` 4.03 MB). | — |
| **Build de Produção do Front-end (`npm run build`)** | **Executado** | Falhou no inlining da fonte `Manrope` (`ENOTFOUND fonts.googleapis.com`) por ausência de rede no sandbox. | — |
| **Runner de Testes do Front-end (`npx ng test --watch=false`)** | **Executado** | Falhou (`No tests found matching **/*.spec.ts, **/*.test.ts`). | Não existem testes escritos no front-end. |
