# Arquitetura de Roteamento - AWS

## Visão Geral

Todos os microservices são expostos através de um **único ponto de entrada** (API Gateway / Load Balancer) usando **prefixos de environment + path** para roteamento. Isso permite organização clara, isolamento por ambiente e facilita o gerenciamento de rotas.

## 🌐 URL Base

### Development
```
https://xxxxx.execute-api.us-east-2.amazonaws.com/dev
```

### Production
```
https://xxxxx.execute-api.us-east-2.amazonaws.com/prod
```

### Local (Kubernetes)
```
http://oficina.local/local
```

## 📍 Mapeamento de Rotas

Cada microservice tem seu **próprio prefixo** na URL, precedido pelo **environment**:

| Environment | Prefixo Service | Serviço | Porta Interna | Descrição |
|-------------|-----------------|---------|---------------|-----------|
| `/dev` | `/eureka` | eureka-server-internal | 8761 | Service Discovery & Swagger Agregado |
| `/dev` | `/auth` | auth-service | 8082 | Autenticação JWT & Usuários |
| `/dev` | `/customer` | customer-service | 8081 | Clientes & Veículos |
| `/dev` | `/catalog` | catalog-service | 8083 | Catálogo de Produtos & Serviços |
| `/dev` | `/inventory` | inventory-service | 8084 | Controle de Estoque |
| `/dev` | `/budget` | budget-service | 8085 | Orçamentos |
| `/dev` | `/work-order` | work-order-service | 8086 | Ordens de Serviço |
| `/dev` | `/notification` | notification-service | 8087 | Notificações (evento-driven) |

*Para produção (`/prod`) e local (`/local`), o padrão é o mesmo, apenas mudando o prefixo do environment.*

## 🔀 Como Funciona o Roteamento

### 1. Nginx Ingress Configuration

O Kubernetes Ingress usa **NGINX** com a seguinte configuração:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
spec:
  rules:
  - host: dev.oficina-mecanica.com
    http:
      paths:
      - path: /auth(/|$)(.*)
        backend:
          service:
            name: auth-service
            port:
              number: 8082
```

### 2. Rewrite Rule Explicado

**Pattern:** `/dev/auth(/|$)(.*)`

- **Prefixo Environment:** `/dev` - Identifica o ambiente
- **Grupo 1:** `(/|$)` - Captura `/` ou fim da string
- **Grupo 2:** `(.*)` - Captura tudo que vem depois

**Rewrite:** `/$2` - Reescreve para apenas o conteúdo do grupo 2

**Exemplos:**

| URL de Entrada | Match Pattern | Env | Service | Path Capturado | URL Reescrita | Destino Final |
|----------------|---------------|-----|---------|----------------|---------------|---------------|
| `/dev/auth/api/auth/login` | ✅ | `dev` | `auth` | `api/auth/login` | `/api/auth/login` | `auth-service:8082/api/auth/login` |
| `/prod/auth/api/auth/login` | ✅ | `prod` | `auth` | `api/auth/login` | `/api/auth/login` | `auth-service:8082/api/auth/login` |
| `/dev/customer/api/clientes/123` | ✅ | `dev` | `customer` | `api/clientes/123` | `/api/clientes/123` | `customer-service:8081/api/clientes/123` |
| `/prod/eureka/swagger-ui.html` | ✅ | `prod` | `eureka` | `swagger-ui.html` | `/swagger-ui.html` | `eureka-server:8761/swagger-ui.html` |

### 3. Fluxo Completo

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │ 1. Request: https://api.com/dev/auth/api/auth/login
       ▼
┌─────────────────┐
│  API Gateway    │
│   (HTTP API)    │
└──────┬──────────┘
       │ 2. Route match: /dev/auth/{proxy+}
       │    Forward via VPC Link
       ▼
┌─────────────────┐
│    VPC Link     │
│ (Private Conn)  │
└──────┬──────────┘
       │ 3. Forward para NLB interno
       ▼
┌─────────────────┐
│ Network Load    │
│    Balancer     │
└──────┬──────────┘
       │ 4. Route para Ingress
       ▼
┌─────────────────┐
│ NGINX Ingress   │
│  Controller     │
└──────┬──────────┘
       │ 5. Match: /dev/auth(/|$)(.*)
       │    Rewrite: /api/auth/login
       ▼
┌─────────────────┐
│  auth-service   │
│   Pod (8082)    │
└─────────────────┘
```

## 📖 Exemplos de Uso

### Auth Service

```bash
# Login - Development
POST https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/auth/api/auth/login

# Login - Production
POST https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/auth/api/auth/login

# Listar usuários - Dev
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/auth/api/usuarios

# Buscar usuário por ID - Prod
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/auth/api/usuarios/1
```

### Customer Service

```bash
# Listar clientes - Dev
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/customer/api/clientes

# Criar cliente - Prod
POST https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/customer/api/clientes

# Listar veículos - Dev
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/customer/api/veiculos

# Buscar veículo por placa - Prod
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/customer/api/veiculos/placa/ABC1234
```

### Catalog Service

```bash
# Listar produtos - Dev
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/catalog/api/catalogo-produtos

# Listar serviços - Prod
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/catalog/api/servicos
```

### Inventory Service

```bash
# Listar estoque - Dev
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/inventory/api/estoque

# Movimentar estoque - Prod
POST https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/inventory/api/estoque/movimentacao
```

### Budget Service

```bash
# Listar orçamentos - Dev
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/budget/api/orcamentos

# Aprovar orçamento - Prod
PUT https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/budget/api/orcamentos/1/aprovar
```

### Work Order Service

```bash
# Listar ordens de serviço - Dev
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/work-order/api/ordens-servico

# Iniciar ordem - Prod
PUT https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/work-order/api/ordens-servico/1/iniciar

# Concluir ordem - Dev
PUT https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/work-order/api/ordens-servico/1/concluir
```

### Eureka Server

```bash
# Dashboard - Dev
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/eureka

# Dashboard - Prod
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/eureka

# Swagger Agregado - Dev
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/eureka/swagger-ui.html

# Health Check - Prod
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/eureka/actuator/health

# API Docs Agregados - Dev
GET https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/eureka/v3/api-docs/swagger-config
```

## 🔒 Autenticação

A maioria dos endpoints requer autenticação JWT. Fluxo:

### 1. Obter Token

```bash
# Development
curl -X POST https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/auth/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Production
curl -X POST https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/auth/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```

### 2. Usar Token nos Requests

```bash
# Development
curl -X GET https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/customer/api/clientes \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Production
curl -X GET https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/customer/api/clientes \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## 🌍 Ambientes

### Development
- **API Gateway:** `https://xxxxx.execute-api.us-east-2.amazonaws.com/dev`
- **Hostname:** `http://dev.oficina-mecanica.com/dev` (requer DNS)
- **Namespace:** `oficina-mecanica-dev`

### Production
- **API Gateway:** `https://xxxxx.execute-api.us-east-2.amazonaws.com/prod`
- **Hostname:** `https://oficina-mecanica.com/prod` (quando configurado)
- **Namespace:** `oficina-mecanica-prod`

### Local
- **Hostname:** `http://oficina.local/local`
- **Namespace:** `oficina`

## 📋 Vantagens dessa Arquitetura

### ✅ Isolamento por Environment
- URLs distintas para dev, staging, prod
- Evita confusão entre ambientes
- Permite testes independentes sem afetar produção
- Facilita rollback e blue-green deployments

### ✅ Organização Clara
- Cada serviço tem seu próprio "namespace" na URL
- Fácil identificar qual serviço e ambiente está sendo acessado
- Padrão consistente: `/{environment}/{service}/{path}`

### ✅ Segurança
- Único ponto de entrada facilita controle de segurança
- Possibilidade de aplicar rate limiting por prefixo e environment
- WAF pode ser aplicado no API Gateway
- Políticas diferentes por environment (dev mais permissivo, prod mais restritivo)

### ✅ Escalabilidade
- Load balancing automático por serviço
- Fácil adicionar novos serviços (basta adicionar novo prefixo)
- Infraestrutura independente por environment

### ✅ Monitoramento
- Logs centralizados no API Gateway
- Métricas por prefixo/serviço/environment
- Tracing distribuído facilitado
- Troubleshooting simplificado por ambiente

### ✅ Versionamento Futuro
Possibilita estratégias de versionamento:
```
/dev/auth/v1/api/auth/login
/dev/auth/v2/api/auth/login
/prod/auth/v1/api/auth/login
```

## 🔧 Configuração no Código

Os microservices **não precisam saber** sobre os prefixos! Os endpoints internos permanecem os mesmos:

### Auth Service (interno)
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(...) {
        // Endpoint interno: /api/auth/login
    }
}
```

### Acesso Externo
```
https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/auth/api/auth/login
                                                      ─────────────────────
                                                      Prefixo do Ingress
```

O Ingress remove o prefixo `/auth` e repassa `/api/auth/login` para o serviço.

## 🧪 Testando Rotas

### Script de Teste

```bash
#!/bin/bash

BASE_URL="https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com"

echo "Testing all services..."

# Auth
curl -s -o /dev/null -w "Auth Service: %{http_code}\n" "$BASE_URL/auth/actuator/health"

# Customer
curl -s -o /dev/null -w "Customer Service: %{http_code}\n" "$BASE_URL/customer/actuator/health"

# Catalog
curl -s -o /dev/null -w "Catalog Service: %{http_code}\n" "$BASE_URL/catalog/actuator/health"

# Inventory
curl -s -o /dev/null -w "Inventory Service: %{http_code}\n" "$BASE_URL/inventory/actuator/health"

# Budget
curl -s -o /dev/null -w "Budget Service: %{http_code}\n" "$BASE_URL/budget/actuator/health"

# Work Order
curl -s -o /dev/null -w "Work Order Service: %{http_code}\n" "$BASE_URL/work-order/actuator/health"

# Eureka
curl -s -o /dev/null -w "Eureka Server: %{http_code}\n" "$BASE_URL/eureka/actuator/health"
```

## 🎯 Swagger Agregado

O Swagger UI no Eureka Server está configurado para usar esses prefixos:

```yaml
# eureka-server/src/main/resources/application-k8s.yml
springdoc:
  swagger-ui:
    urls:
      - name: Auth Service
        url: https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/auth/v3/api-docs
      - name: Customer Service
        url: https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/customer/v3/api-docs
      # ... etc
```

Acesse: https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/eureka/swagger-ui.html

## 📚 Referências

- [Nginx Ingress Rewrite](https://kubernetes.github.io/ingress-nginx/examples/rewrite/)
- [Kubernetes Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/)
- [AWS API Gateway](https://docs.aws.amazon.com/apigateway/)

## 🔄 Mudanças Futuras

### Possível Migração para Service Mesh
No futuro, pode-se considerar migrar para um service mesh (Istio, Linkerd) que oferece:
- Roteamento mais sofisticado
- Retry automático
- Circuit breaker
- mTLS entre serviços

### API Gateway Dedicado
Considerar Kong, Ambassador, ou AWS API Gateway nativo para:
- Rate limiting avançado
- Autenticação centralizada
- Transformações de payload
- Monetização de APIs
