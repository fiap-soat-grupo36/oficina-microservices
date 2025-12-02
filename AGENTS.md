# Repository Guidelines

## Project Structure & Module Organization
- Root `pom.xml` is the aggregator; each microservice (eureka-server, auth-service, customer-service, catalog-service, inventory-service, budget-service, work-order-service, notification-service) has its own `pom.xml`, `src/main` and `src/test`.
- `shared-library` holds cross-cutting code (DTOs, validators, security helpers, exceptions, MapStruct mappers, constants). Favor reusing these types instead of redefining them per service.
- Container assets live in each service’s `Dockerfile`; orchestration sits in `docker-compose.yml` and `k8s/`.

## Build, Test, and Development Commands
- `mvn clean install -DskipTests` (root): builds all modules and installs shared-library locally.
- `mvn test` (root or service): runs unit and slice tests with profile `test`.
- `mvn verify -Pverify-coverage` (root): runs JaCoCo check with 80% minimum (excludes DTOs, entities, config, mappers).
- `mvn spring-boot:run` inside a service: starts that service locally; start `eureka-server` first.
- `docker-compose up -d` / `docker-compose down`: build and run all services in containers.

## Coding Style & Naming Conventions
- Java 21, Spring Boot 3.5; use 4-space indentation and keep code UTF-8.
- Package pattern `br.com.fiap.oficina.<service>.<layer>`; controllers under `controller`, business logic in `service`/`impl`, persistence in `repository`, mapping in `mapper` (MapStruct), shared data in `dto`/`vo`.
- Prefer Lombok for boilerplate (`@Getter`, `@Builder`) and MapStruct for conversions; annotate inputs with `@Valid` and rely on shared exception handlers.
- REST endpoints live under `/api/...`; keep method names imperative and avoid business logic in controllers.

## Testing Guidelines
- JUnit 5 with Mockito and Spring Boot test slices (e.g., `@WebMvcTest`, MockMvc). Name tests `*Test` and use descriptive `@DisplayName` in Portuguese as in existing suites.
- Activate profile `test` for integration-style tests; mock external calls and JWT generation where possible.
- Local check: `mvn test`. For PRs, use `mvn verify -Pverify-coverage`; CI enforces ≥50% coverage on PRs and ≥70% on main.

## Commit & Pull Request Guidelines
- Branches: use `feature/<topic>` or `hotfix/<topic>`; CI auto-creates PRs to `develop` and `main` based on workflows.
- Commit messages follow the current history: short, imperative English/Portuguese summaries (e.g., "Add automated PR creation").
- Before opening a PR, ensure `mvn test` (or `verify`) passes and new endpoints are documented via Swagger annotations.
- PRs should link issues, describe changes/impacts, and include config, DB, or screenshot notes relevant to reviewers.

## Environment & Security Tips
- Configure service URLs via env vars (e.g., `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`); do not hardcode secrets in `application.properties`.
- Shared CORS and security helpers live in `shared-library`; reuse them instead of duplicating config per service.
