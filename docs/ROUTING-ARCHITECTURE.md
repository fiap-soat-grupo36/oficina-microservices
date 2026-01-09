# Arquitetura de Roteamento - AWS

## Visão Geral

Todos os microservices são expostos através de um **único ponto de entrada** (API Gateway / Load Balancer) usando **prefixos de path** para roteamento. Isso permite organização clara e facilita o gerenciamento de rotas.

## 🌐 URL Base

```
https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com
```

## 📍 Mapeamento de Rotas

Cada microservice tem seu **próprio prefixo** na URL:

| Prefixo | Serviço | Porta Interna | Descrição |
|---------|---------|---------------|-----------|
| `/eureka` | eureka-server-internal | 8761 | Service Discovery & Swagger Agregado |
| `/auth` | auth-service | 8082 | Autenticação JWT & Usuários |
| `/customer` | customer-service | 8081 | Clientes & Veículos |
| `/catalog` | catalog-service | 8083 | Catálogo de Produtos & Serviços |
| `/inventory` | inventory-service | 8084 | Controle de Estoque |
| `/budget` | budget-service | 8085 | Orçamentos |
| `/work-order` | work-order-service | 8086 | Ordens de Serviço |
| `/notification` | notification-service | 8087 | Notificações (evento-driven) |

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

**Pattern:** `/auth(/|$)(.*)`

- **Grupo 1:** `(/|$)` - Captura `/` ou fim da string
- **Grupo 2:** `(.*)` - Captura tudo que vem depois

**Rewrite:** `/$2` - Reescreve para apenas o conteúdo do grupo 2

**Exemplos:**

| URL de Entrada | Match Pattern | Grupo 1 | Grupo 2 | URL Reescrita | Destino Final |
|----------------|---------------|---------|---------|---------------|---------------|
| `/auth/api/auth/login` | ✅ | `/` | `api/auth/login` | `/api/auth/login` | `auth-service:8082/api/auth/login` |
| `/auth` | ✅ | (vazio) | (vazio) | `/` | `auth-service:8082/` |
| `/customer/api/clientes/123` | ✅ | `/` | `api/clientes/123` | `/api/clientes/123` | `customer-service:8081/api/clientes/123` |
| `/eureka/swagger-ui.html` | ✅ | `/` | `swagger-ui.html` | `/swagger-ui.html` | `eureka-server:8761/swagger-ui.html` |

### 3. Fluxo Completo

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │ 1. Request: https://api.com/auth/api/auth/login
       ▼
┌─────────────────┐
│  API Gateway    │
│   (AWS ALB)     │
└──────┬──────────┘
       │ 2. Forward para Load Balancer
       ▼
┌─────────────────┐
│ Load Balancer   │
│  (AWS NLB/ELB)  │
└──────┬──────────┘
       │ 3. Route para Ingress Controller
       ▼
┌─────────────────┐
│ NGINX Ingress   │
│  Controller     │
└──────┬──────────┘
       │ 4. Match: /auth(/|$)(.*)
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
# Login
POST https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/auth/api/auth/login

# Listar usuários
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/auth/api/usuarios

# Buscar usuário por ID
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/auth/api/usuarios/1
```

### Customer Service

```bash
# Listar clientes
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/customer/api/clientes

# Criar cliente
POST https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/customer/api/clientes

# Listar veículos
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/customer/api/veiculos

# Buscar veículo por placa
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/customer/api/veiculos/placa/ABC1234
```

### Catalog Service

```bash
# Listar produtos
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/catalog/api/catalogo-produtos

# Listar serviços
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/catalog/api/servicos
```

### Inventory Service

```bash
# Listar estoque
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/inventory/api/estoque

# Movimentar estoque
POST https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/inventory/api/estoque/movimentacao
```

### Budget Service

```bash
# Listar orçamentos
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/budget/api/orcamentos

# Aprovar orçamento
PUT https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/budget/api/orcamentos/1/aprovar
```

### Work Order Service

```bash
# Listar ordens de serviço
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/work-order/api/ordens-servico

# Iniciar ordem
PUT https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/work-order/api/ordens-servico/1/iniciar

# Concluir ordem
PUT https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/work-order/api/ordens-servico/1/concluir
```

### Eureka Server

```bash
# Dashboard
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/eureka

# Swagger Agregado
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/eureka/swagger-ui.html

# Health Check
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/eureka/actuator/health

# API Docs Agregados
GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/eureka/v3/api-docs/swagger-config
```

## 🔒 Autenticação

A maioria dos endpoints requer autenticação JWT. Fluxo:

### 1. Obter Token

```bash
curl -X POST https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/auth/api/auth/login \
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
curl -X GET https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/customer/api/clientes \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## 🌍 Ambientes

### Development (dev.oficina-mecanica.com)

- **API Gateway:** `https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com`
- **Load Balancer:** `http://k8s-ingressn-ingressn-485fd1044e-21c906f882317f33.elb.us-east-2.amazonaws.com`
- **Hostname:** `http://dev.oficina-mecanica.com` (requer DNS)
- **Namespace:** `oficina-mecanica-dev`

### Production (oficina-mecanica.com)

- **Hostname:** `https://oficina-mecanica.com` (quando configurado)
- **Namespace:** `oficina-mecanica-prod`

## 📋 Vantagens dessa Arquitetura

### ✅ Organização Clara
- Cada serviço tem seu próprio "namespace" na URL
- Fácil identificar qual serviço está sendo acessado

### ✅ Segurança
- Único ponto de entrada facilita controle de segurança
- Possibilidade de aplicar rate limiting por prefixo
- WAF pode ser aplicado no API Gateway

### ✅ Escalabilidade
- Load balancing automático por serviço
- Fácil adicionar novos serviços (basta adicionar novo prefixo)

### ✅ Monitoramento
- Logs centralizados no API Gateway
- Métricas por prefixo/serviço
- Tracing distribuído facilitado

### ✅ Versionamento Futuro
Possibilita estratégias de versionamento:
```
/auth/v1/api/auth/login
/auth/v2/api/auth/login
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
