# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Oficina Microservices is a Spring Boot-based microservices architecture for a car workshop management system. The system uses Netflix Eureka for service discovery, OpenFeign for inter-service communication, and PostgreSQL as the shared database.

## Technology Stack

- **Java 21** - Language version
- **Spring Boot 3.5.3** - Application framework
- **Spring Cloud 2025.0.0** - Cloud patterns (Eureka, OpenFeign)
- **Maven 3.9+** - Build tool (multi-module project)
- **PostgreSQL 15** - Database
- **Docker & Docker Compose** - Containerization
- **Kubernetes** - Orchestration
- **JWT (jjwt 0.12.6)** - Authentication
- **Lombok 1.18.30** - Code generation
- **MapStruct 1.5.5.Final** - Object mapping
- **Springdoc OpenAPI 2.8.9** - API documentation

## Build & Test Commands

### Maven Operations

```bash
# Build all modules (from root)
mvn clean install -DskipTests

# Run tests for all modules
mvn test

# Run tests for a specific module
cd <service-name>
mvn test

# Run a single test class
mvn test -Dtest=ClassNameTest

# Run a single test method
mvn test -Dtest=ClassNameTest#methodName

# Generate JaCoCo coverage reports
mvn verify

# Verify code coverage meets 80% threshold
mvn verify -Pverify-coverage

# Run with test profile (uses H2 in-memory database)
mvn test -Dspring.profiles.active=test
```

### Docker Operations

```bash
# Development: Build and run all services locally
docker compose --profile dev up -d

# Rebuild after code changes
docker compose --profile dev up -d --build

# View logs
docker compose --profile dev logs -f [service-name]

# Stop all services
docker compose --profile dev down

# Clean everything (containers, volumes, images)
docker compose --profile dev down -v --rmi all

# Production: Use published images
REGISTRY=<username> TAG=latest docker compose --profile prod -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Docker Image Publishing (Makefile)

```bash
# Build all service images locally
make build TAG=latest

# Build and push to Docker Hub (requires REGISTRY)
make push REGISTRY=<your-dockerhub-username> TAG=latest

# Multi-architecture build and push
make buildx-push REGISTRY=<username> TAG=latest PLATFORMS='linux/amd64,linux/arm64'

# Clean local images
make clean TAG=latest
```

### Kubernetes Operations

The project uses **Kustomize overlays** to support different environments:
- `overlays/local` - Minikube (namespace: `oficina`)
- `overlays/dev` - EKS Dev (namespace: `oficina-mecanica-dev`)
- `overlays/prod` - EKS Prod (namespace: `oficina-mecanica-prod`)

#### Local Development (Minikube)

```bash
# Start Minikube
minikube start --cpus=4 --memory=8192

# Enable addons
minikube addons enable ingress

# Deploy to local
kubectl apply -k k8s/overlays/local

# Watch pod status
kubectl -n oficina get pods -w

# Port-forward services
kubectl -n oficina port-forward svc/eureka-server 8761:8761

# Access via ingress (add to /etc/hosts)
echo "$(minikube ip) oficina.local" | sudo tee -a /etc/hosts

# Clean up
kubectl delete -k k8s/overlays/local
```

#### EKS Development via Terraform

```bash
# Navigate to infra directory
cd infra

# Select dev workspace
terraform workspace select dev

# Apply infrastructure (automatically deploys k8s manifests)
terraform apply

# Or deploy manually
aws eks update-kubeconfig --name eks-fiap-oficina-mecanica --region us-east-2
kubectl apply -k k8s/overlays/dev

# View pods
kubectl -n oficina-mecanica-dev get pods -w
```

#### EKS Production via Terraform

```bash
cd infra
terraform workspace select prod
terraform apply

# Or deploy manually
kubectl apply -k k8s/overlays/prod
kubectl -n oficina-mecanica-prod get pods -w
```

#### Preview Kustomize Output

```bash
# See what will be applied without actually applying
kubectl kustomize k8s/overlays/local
kubectl kustomize k8s/overlays/dev
kubectl kustomize k8s/overlays/prod
```

## Architecture

### Service Registry Pattern

All microservices register with **Eureka Server** (port 8761) for service discovery. Services communicate using logical names (e.g., `customer-service`) rather than hardcoded URLs.

- Eureka dashboard: http://localhost:8761
- Registration happens automatically via Spring Cloud Netflix Eureka Client
- Health checks use `/actuator/health` endpoints
- Self-preservation is disabled for faster dev cycles

### Microservices

1. **eureka-server** (8761) - Service registry and discovery
2. **auth-service** (8082) - JWT authentication and user management
3. **customer-service** (8081) - Customer and vehicle management
4. **catalog-service** (8083) - Service and product catalog
5. **inventory-service** (8084) - Inventory control
6. **budget-service** (8085) - Budget management
7. **work-order-service** (8086) - Work order management
8. **notification-service** (8087) - Email notifications

### Service Communication

Services use **OpenFeign** clients for synchronous REST communication:

```java
@FeignClient(name = "customer-service", contextId = "cliente-client")
public interface ClienteClient {
    @GetMapping("/api/clientes/{id}")
    ClienteResponseDTO getCliente(@PathVariable("id") Long id);
}
```

- Service names must match those registered in Eureka
- Use unique `contextId` when multiple Feign clients target the same service
- Feign clients are defined in each service's `client/` package

### Shared Library

The `shared-library` module contains cross-cutting concerns:

- **Security**: `JwtTokenProvider`, `JwtAuthenticationFilter`
- **Configuration**: `CorsConfig`
- **Exception handling**: `GlobalExceptionHandler`
- **Value Objects**: `Endereco` (Address)
- **Enums**: `Role`, `StatusOrcamento`, `StatusOrdemServico`, etc.
- **Constants**: `MensagemDeErroConstants`
- **DTOs**: Shared request/response objects

All services depend on `shared-library` via Maven dependency.

### Database Strategy

- **Single PostgreSQL instance** shared across all services (not ideal for production, but used for simplicity)
- Each service defines its own entities and repositories
- Connection details configured via environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- DDL mode controlled via `DDL_AUTO` (create-drop for dev, update for prod)
- Seed data controlled via `SQL_INIT_MODE` (always for dev, never for prod)

### Environment Profiles

Each service supports multiple Spring profiles:

- **local** - Local development (default)
- **docker** - Docker Compose environment
- **k8s** - Kubernetes environment
- **test** - Unit testing (uses H2 in-memory DB)

Profile-specific configurations are in `application-{profile}.yml` files.

### Authentication & Authorization

- JWT tokens generated by `auth-service`
- Token provider in `shared-library` (`JwtTokenProvider`)
- Tokens include username and roles
- Secret and expiration configured via: `JWT_SECRET`, `JWT_EXPIRATION`
- Filter: `JwtAuthenticationFilter` validates tokens on protected endpoints

### API Documentation

**Aggregated Swagger UI** at Eureka Server: http://localhost:8761/swagger-ui.html

This single interface exposes all service APIs via a dropdown selector.

Individual service docs:
- Auth: http://localhost:8082/swagger-ui.html
- Customer: http://localhost:8081/swagger-ui.html
- Catalog: http://localhost:8083/swagger-ui.html
- Inventory: http://localhost:8084/swagger-ui.html
- Budget: http://localhost:8085/swagger-ui.html
- Work Order: http://localhost:8086/swagger-ui.html

## Project Structure

```
oficina-microservices/
├── pom.xml                     # Parent POM (multi-module aggregator)
├── shared-library/             # Common code (security, DTOs, enums, exceptions)
├── eureka-server/              # Service registry
├── auth-service/               # Authentication
├── customer-service/           # Customers & vehicles
├── catalog-service/            # Products & services catalog
├── inventory-service/          # Inventory management
├── budget-service/             # Budget management
├── work-order-service/         # Work orders
├── notification-service/       # Email notifications
├── docker-compose.yml          # Dev profile (builds from source)
├── docker-compose.prod.yml     # Prod profile overrides (uses published images)
├── Makefile                    # Docker image build/push automation
├── k8s/                        # Kubernetes manifests (Kustomize-based)
│   ├── base/                   # Base manifests (common to all environments)
│   │   ├── kustomization.yaml
│   │   ├── postgres.yaml
│   │   ├── eureka-server.yaml
│   │   ├── *-service.yaml      # 7 microservices deployments
│   │   ├── configmap-shared.yaml
│   │   └── hpa.yaml
│   └── overlays/               # Environment-specific overlays
│       ├── local/              # Minikube (namespace: oficina)
│       │   ├── kustomization.yaml
│       │   ├── namespace.yaml
│       │   ├── secret-*.yaml   # Hardcoded secrets (dev only)
│       │   └── ingress.yaml    # oficina.local
│       ├── dev/                # EKS Dev (namespace: oficina-mecanica-dev)
│       │   ├── kustomization.yaml
│       │   ├── namespace.yaml
│       │   ├── secrets.yaml
│       │   └── ingress.yaml    # dev.oficina-mecanica.com
│       └── prod/               # EKS Prod (namespace: oficina-mecanica-prod)
│           ├── kustomization.yaml
│           ├── namespace.yaml
│           ├── secrets.yaml    # Use AWS Secrets Manager!
│           ├── ingress.yaml    # oficina-mecanica.com
│           └── patches/        # Prod-specific patches
│               ├── replicas.yaml    # 2 base replicas
│               ├── resources.yaml   # Higher CPU/memory
│               └── hpa.yaml         # HPA: min 2, max 5
└── infra/                      # Terraform infrastructure code
    ├── providers.tf
    ├── backend.tf              # S3 backend
    ├── data.tf                 # References existing EKS cluster
    ├── namespace.tf            # Creates k8s namespace
    ├── k8s.tf                  # Deploys k8s manifests via Kustomize
    ├── datadog.tf              # Monitoring
    └── environments/
        ├── dev.tfvars
        └── prod.tfvars
```

### Service Code Structure

Each microservice follows this pattern:

```
<service-name>/
├── src/
│   ├── main/
│   │   ├── java/br/com/fiap/oficina/<service>/
│   │   │   ├── <Service>Application.java   # Main class
│   │   │   ├── config/                      # Configuration classes
│   │   │   ├── controller/                  # REST controllers
│   │   │   ├── dto/                         # Request/Response DTOs
│   │   │   ├── entity/                      # JPA entities
│   │   │   ├── repository/                  # Spring Data repositories
│   │   │   ├── service/                     # Business logic
│   │   │   ├── mapper/                      # MapStruct mappers
│   │   │   └── client/                      # Feign clients (if any)
│   │   └── resources/
│   │       ├── application.yml              # Default config
│   │       ├── application-local.yml        # Local profile
│   │       ├── application-docker.yml       # Docker profile
│   │       ├── application-k8s.yml          # Kubernetes profile
│   │       └── import.sql                   # Seed data (optional)
│   └── test/
│       ├── java/                            # Unit & integration tests
│       └── resources/
│           └── application-test.yml         # Test profile (H2 DB)
├── Dockerfile
├── README.md
└── pom.xml
```

## Testing Conventions

- Tests located in `src/test/java/` with same package structure as main code
- Test classes end with `Test` suffix (e.g., `ClienteServiceImplTest`)
- Controller tests: `*ControllerTest`
- Service tests: `*ServiceImplTest`
- Use `@SpringBootTest` for integration tests
- Use `@WebMvcTest` for controller tests
- Test profile automatically uses H2 in-memory database
- Coverage goal: 80% line coverage (enforced via `verify-coverage` profile)

## CI/CD Workflows

### GitHub Actions Pipelines

- **ci.yml** - Feature/hotfix branches: build, test, SonarCloud analysis, Docker validation
- **develop.yml** - Develop branch: full CI + Docker push with `develop` tag
- **main.yml** - Main branch: full CI + Docker push with `latest` tag
- **cd.yml** - Continuous deployment (Terraform apply)

### Reusable Workflows

- **_reusable-changes.yml** - Detects what changed (modules, Terraform, Docker)
- **_reusable-build-cache.yml** - Builds once and caches for parallel test jobs
- **_reusable-test.yml** - Runs tests in parallel using cached build
- **_reusable-sonar.yml** - SonarCloud analysis
- **_reusable-dockerhub.yml** - Builds and pushes Docker images
- **_reusable-terraform.yml** - Terraform plan/apply
- **_reusable-create-pr.yml** - Auto-creates PRs to develop

### Important CI/CD Details

- Tests run with `spring.profiles.active=test`
- Docker builds only pushed on develop/main branches (not on feature branches)
- SonarCloud exclusions: DTOs, entities, configs, mappers, main classes
- JaCoCo coverage reports generated during `verify` phase
- Multi-module Maven project requires building from root: `mvn clean install`

## Common Patterns

### Adding a New Microservice

1. Add module to root `pom.xml` `<modules>` section
2. Create service directory with standard structure
3. Add `shared-library` dependency in service POM
4. Register with Eureka via `@EnableEurekaClient` or auto-configuration
5. Configure `application.yml` with service name and port
6. Add Dockerfile using multi-stage build pattern (see existing services)
7. Add service to `docker-compose.yml` (dev profile)
8. Add deployment/service manifests to `k8s/`
9. Update `kustomization.yaml` with new manifests
10. Add service to Makefile `SERVICES` list
11. Add Swagger URL to `eureka-server/application.yml`

### Adding Feign Client

1. Create interface in `client/` package
2. Annotate with `@FeignClient(name = "service-name", contextId = "unique-id")`
3. Define REST methods matching target service endpoints
4. Inject and use in service layer
5. Enable Feign clients via `@EnableFeignClients` (usually on main class)

### Lombok & MapStruct Integration

- Lombok annotation processor must run **before** MapStruct
- Configured in parent POM `maven-compiler-plugin` `annotationProcessorPaths`
- Use `lombok-mapstruct-binding` to ensure compatibility
- Mappers use `componentModel = "spring"` for dependency injection

### Exception Handling

Global exception handler in `shared-library` (`GlobalExceptionHandler`) catches common exceptions:
- Custom business exceptions (e.g., `EstoqueInsuficienteException`)
- Validation errors
- ResourceNotFoundExceptions
- Returns standardized error responses

## Kustomize Multi-Environment Strategy

The project uses **Kustomize overlays** to manage deployments across 3 environments:

### Environment Comparison

| Feature | Local (Minikube) | Dev (EKS) | Prod (EKS) |
|---------|------------------|-----------|------------|
| Namespace | `oficina` | `oficina-mecanica-dev` | `oficina-mecanica-prod` |
| Managed By | Manual kubectl | Terraform | Terraform |
| Secrets | Hardcoded (safe) | Hardcoded (change!) | AWS Secrets Manager |
| Ingress Host | oficina.local | dev.oficina-mecanica.com | oficina-mecanica.com |
| Base Replicas | 1 | 1 | 2 |
| HPA Range | 1-2 | 1-2 | 2-5 |
| CPU Request | 150m | 150m | 250m |
| Memory Request | 256Mi | 256Mi | 512Mi |
| CPU Limit | 500m | 500m | 1000m |
| Memory Limit | 1Gi | 1Gi | 2Gi |

### Deployment Flow

**Local Development:**
```bash
kubectl apply -k k8s/overlays/local
```

**EKS via Terraform:**
```bash
cd infra
terraform workspace select dev  # or prod
terraform apply  # Automatically applies correct overlay
```

**Manual EKS Deploy:**
```bash
kubectl apply -k k8s/overlays/dev   # or prod
```

### Secrets Management by Environment

- **Local**: Hardcoded in YAML (acceptable for local dev)
- **Dev**: Hardcoded in YAML (MUST change default values)
- **Prod**: **Use AWS Secrets Manager + External Secrets Operator** (see k8s/README-OVERLAYS.md)

## Important Notes

- **Service startup order matters**: Start Eureka first, then other services
- **Eureka registration delay**: Services may take 30+ seconds to register
- **Database migrations**: Controlled via `DDL_AUTO` and `SQL_INIT_MODE` environment variables
- **JWT secret**: Must be at least 256 bits for HS256 algorithm
- **CORS**: Globally configured in `shared-library/CorsConfig`
- **Port conflicts**: Ensure ports 8081-8087 and 8761 are available
- **Resource requirements**: Minimum 8GB RAM for running all services via Docker/K8s
- **Kubernetes namespaces**: Different per environment (oficina vs oficina-mecanica-dev/prod)
- **Terraform workspace**: Use `dev` or `prod` workspace to deploy to correct environment
- **Kustomize overlays**: Always specify the correct overlay path when deploying
- **Production secrets**: Never commit real production secrets - use AWS Secrets Manager
- **Docker Buildx**: For multi-arch builds, create builder: `docker buildx create --name multi --use`

## Troubleshooting

### Service won't register with Eureka
- Check Eureka is running: `curl http://localhost:8761/actuator/health`
- Verify `EUREKA_URL` environment variable
- Check service logs for connection errors
- Wait 30 seconds for registration to complete

### Feign client errors
- Ensure target service is registered in Eureka
- Verify service name matches Eureka registration
- Check contextId is unique if multiple clients target same service
- Review Feign client logs for HTTP errors

### Database connection issues
- Verify PostgreSQL is running and healthy
- Check `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` environment variables
- Ensure database `oficina-db` exists
- Review service logs for JDBC connection errors

### Maven build failures
- Run from project root: `mvn clean install`
- Ensure Java 21 is active: `java -version`
- Clear local repo: `rm -rf ~/.m2/repository/br/com/fiap/oficina`
- Check annotation processor paths are correct for Lombok/MapStruct

### Test failures
- Verify test profile uses H2: check `application-test.yml`
- Ensure no port conflicts during test execution
- Run single test to isolate: `mvn test -Dtest=ClassName#method`
- Check for environment-specific issues (mocks, test data)
