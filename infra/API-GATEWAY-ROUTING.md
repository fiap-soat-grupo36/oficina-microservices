    `# Configuração de Rotas com Prefixo de Environment

## 📋 Visão Geral

Este repositório cria a **infraestrutura completa por environment**, incluindo:
- Kubernetes Ingress com rotas prefixadas (`/dev`, `/prod`, `/local`)
- Network Load Balancer (criado automaticamente pelo NGINX Ingress)
- VPC Link
- **API Gateway HTTP v2 dedicado** com rotas `/{environment}/{service}/{path}`

Cada deploy (dev ou prod) cria sua própria stack completa e isolada.

## 🌐 Estrutura de URLs

### Padrão Base
```
https://{api-gateway-endpoint}/{environment}/{service}/{path}
```

### Exemplos por Environment

#### Development (dev)
```
https://api-gateway.com/dev/eureka/
https://api-gateway.com/dev/auth/api/auth/login
https://api-gateway.com/dev/customer/api/clientes
```

#### Production (prod)
```
https://api-gateway.com/prod/eureka/
https://api-gateway.com/prod/auth/api/auth/login
https://api-gateway.com/prod/customer/api/clientes
```

#### Local
```
http://oficina.local/local/eureka/
http://oficina.local/local/auth/api/auth/login
```

## 📍 Mapeamento de Rotas no Kubernetes Ingress

Cada serviço está configurado no Ingress com o prefixo do environment:

| Environment | Service Path | Microservice | Porta | Descrição |
|-------------|--------------|--------------|-------|-----------|
| `/dev` | `/eureka` | eureka-server-internal | 8761 | Service Discovery & Swagger |
| `/dev` | `/auth` | auth-service | 8082 | Autenticação JWT |
| `/dev` | `/customer` | customer-service | 8081 | Clientes & Veículos |
| `/dev` | `/catalog` | catalog-service | 8083 | Catálogo de Produtos |
| `/dev` | `/inventory` | inventory-service | 8084 | Controle de Estoque |
| `/dev` | `/budget` | budget-service | 8085 | Orçamentos |
| `/dev` | `/work-order` | work-order-service | 8086 | Ordens de Serviço |
| `/dev` | `/notification` | notification-service | 8087 | Notificações |

*Para produção (`/prod`) e local (`/local`), o padrão é o mesmo, apenas mudando o prefixo.*

## 🔀 Configuração dos Ingress

### Development ([k8s/overlays/dev/ingress.yaml](../../k8s/overlays/dev/ingress.yaml))

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: oficina-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
spec:
  rules:
    - host: dev.oficina-mecanica.com
      http:
        paths:
          - path: /dev/eureka(/|$)(.*)
            backend:
              service:
                name: eureka-server-internal
                port:
                  number: 8761
          # ... outras rotas
```

### Production ([k8s/overlays/prod/ingress.yaml](../../k8s/overlays/prod/ingress.yaml))

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: oficina-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
spec:
  rules:
    - host: oficina-mecanica.com
      http:
        paths:
          - path: /prod/eureka(/|$)(.*)
            backend:
              service:
                name: eureka-server-internal
                port:
                  number: 8761
          # ... outras rotas
```

## 🔧 Outputs do Terraform

Após aplicar o Terraform, você terá acesso aos seguintes outputs úteis para configurar o API Gateway externo:

```bash
# Environment atual
terraform output environment
# Output: dev (ou prod)

# Base path para configurar no API Gateway
terraform output ingress_base_path
# Output: /dev (ou /prod)

# VPC Link ID (para conectar API Gateway ao NLB)
terraform output vpc_link_id

# NLB DNS Name (target para o API Gateway)
terraform output nlb_dns_name
```

## 🚀 Deploy

### 1. Aplicar Terraform

```bash
cd infra

# Para development
terraform workspace select dev
terraform apply -var-file=environments/dev.tfvars

# Para production
terraform workspace select prod
terraform apply -var-file=environments/prod.tfvars

# Obter informações
terraform output environment
terraform output ingress_base_path
terraform output nlb_dns_name
```

### 2. Aplicar Kubernetes Ingress

```bash
# Development
kubectl apply -k k8s/overlays/dev

# Production
kubectl apply -k k8s/overlays/prod

# Verificar
kubectl get ingress -n oficina
```

## 🔗 Integração com API Gateway Externo

O API Gateway (gerenciado em outro repositório) deve ser configurado para:

1. **Usar o VPC Link** criado aqui para conectar ao NLB interno
2. **Configurar rotas** com os prefixos de environment:
   - `/{environment}/eureka/{proxy+}` → NLB
   - `/{environment}/auth/{proxy+}` → NLB
   - `/{environment}/customer/{proxy+}` → NLB
   - etc.

3. **Passar o path completo** para o NLB, que então roteia via Ingress para os serviços

### Exemplo de Configuração no API Gateway (referência)

```terraform
# No repositório do API Gateway
resource "aws_apigatewayv2_route" "service_route" {
  api_id    = aws_apigatewayv2_api.main.id
  route_key = "ANY /${var.environment}/{service}/{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.nlb.id}"
}
```

## 🔄 Fluxo de Requisição

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │ 1. https://api.com/dev/auth/api/auth/login
       ▼
┌─────────────────────┐
│   API Gateway       │
│ (Repo Externo)      │
└──────┬──────────────┘
       │ 2. Route: /dev/auth/{proxy+}
       │    Via VPC Link
       ▼
┌─────────────────────┐
│ Network Load        │
│    Balancer         │
│ (Criado aqui)       │
└──────┬──────────────┘
       │ 3. Forward: /dev/auth/api/auth/login
       ▼
┌─────────────────────┐
│  NGINX Ingress      │
│   Controller        │
│ (Configurado aqui)  │
└──────┬──────────────┘
       │ 4. Match: /dev/auth(/|$)(.*)
       │    Rewrite: /api/auth/login
       ▼
┌─────────────────────┐
│   auth-service      │
│    Pod (8082)       │
└─────────────────────┘
```

## 📖 Exemplos de Uso

### Auth Service

```bash
# Login - Development
POST https://api-gateway.com/dev/auth/api/auth/login

# Login - Production
POST https://api-gateway.com/prod/auth/api/auth/login
```

### Customer Service

```bash
# Listar clientes - Dev
GET https://api-gateway.com/dev/customer/api/clientes

# Criar cliente - Prod
POST https://api-gateway.com/prod/customer/api/clientes
```

### Eureka Dashboard

```bash
# Dashboard - Dev
GET https://api-gateway.com/dev/eureka/

# Dashboard - Prod
GET https://api-gateway.com/prod/eureka/
```

## 🎯 Benefícios

1. **Isolamento por Environment**
   - URLs distintas para dev e prod
   - Evita confusão entre ambientes
   - Permite testes independentes

2. **Gestão Separada**
   - API Gateway gerenciado centralmente
   - Ingress e NLB por aplicação
   - Facilita múltiplos times

3. **Segurança**
   - Controle granular por environment
   - VPC Link para comunicação privada
   - Políticas diferentes por ambiente

4. **Flexibilidade**
   - Fácil adicionar novos services
   - Suporta múltiplas aplicações
   - Versionamento simplificado

## 📝 Notas Importantes

- O **environment é derivado automaticamente** do Terraform workspace (`dev`, `prod`, etc.)
- Os **Ingress** neste repositório expõem as rotas com prefixo de environment
- O **API Gateway externo** deve ser configurado para rotear baseado nesses prefixos
- O **VPC Link** conecta o API Gateway ao NLB interno criado aqui
- Os **microserviços não precisam saber** sobre os prefixos - o Ingress faz o rewrite


## 🌐 Estrutura de URLs

### Padrão Base
```
https://{api-gateway-endpoint}/{environment}/{service}/{path}
```

### Exemplos por Environment

#### Development (dev)
```
https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/eureka/
https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/auth/api/auth/login
https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/customer/api/clientes
```

#### Production (prod)
```
https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/eureka/
https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/auth/api/auth/login
https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/customer/api/clientes
```

## 📍 Mapeamento de Rotas

Cada serviço tem duas rotas configuradas (raiz e com proxy):

| Environment Prefix | Service Path | Microservice | Descrição |
|-------------------|--------------|--------------|-----------|
| `/{env}` | `/eureka` | eureka-server | Service Discovery & Swagger |
| `/{env}` | `/auth` | auth-service | Autenticação JWT |
| `/{env}` | `/customer` | customer-service | Clientes & Veículos |
| `/{env}` | `/catalog` | catalog-service | Catálogo de Produtos |
| `/{env}` | `/inventory` | inventory-service | Controle de Estoque |
| `/{env}` | `/budget` | budget-service | Orçamentos |
| `/{env}` | `/work-order` | work-order-service | Ordens de Serviço |
| `/{env}` | `/notification` | notification-service | Notificações |

## 🔀 Configuração Técnica

### Recursos Terraform Criados

1. **API Gateway HTTP API** (`aws_apigatewayv2_api.oficina`)
   - Protocolo HTTP
   - CORS habilitado
   - Nome: `oficina-api-{environment}`

2. **Stage** (`aws_apigatewayv2_stage.oficina`)
   - Auto-deploy habilitado
   - Throttling: 5000 burst / 2000 rate limit
   - Stage: `$default`

3. **VPC Link Integration** (`aws_apigatewayv2_integration.eureka`)
   - Tipo: HTTP_PROXY
   - Método: ANY
   - Conexão via VPC Link ao NLB interno

4. **Rotas** (2 por serviço: raiz + proxy)
   - Formato: `ANY /{environment}/{service}`
   - Formato: `ANY /{environment}/{service}/{proxy+}`

### Variáveis Dinâmicas

O environment é definido automaticamente baseado no Terraform workspace:

```terraform
locals {
  environment = terraform.workspace == "default" ? "dev" : terraform.workspace
}
```

## 📖 Exemplos de Uso

### Auth Service

```bash
# Login - Dev
curl -X POST https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/auth/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"senha123"}'

# Login - Prod
curl -X POST https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/auth/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"senha123"}'
```

### Customer Service

```bash
# Listar clientes - Dev
curl https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/customer/api/clientes \
  -H "Authorization: Bearer {token}"

# Criar cliente - Prod
curl -X POST https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/customer/api/clientes \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"nome":"João Silva","email":"joao@example.com"}'
```

### Eureka Dashboard

```bash
# Dashboard - Dev
open https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/eureka/

# Dashboard - Prod
open https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/eureka/
```

### Swagger UI

```bash
# Swagger - Dev
open https://xxxxx.execute-api.us-east-2.amazonaws.com/dev/eureka/swagger-ui.html

# Swagger - Prod
open https://xxxxx.execute-api.us-east-2.amazonaws.com/prod/eureka/swagger-ui.html
```

## 🚀 Deploy

### 1. Selecionar Workspace

```bash
# Para development
terraform workspace select dev

# Para production
terraform workspace select prod
```

### 2. Aplicar Configuração

```bash
# Development
terraform apply -var-file=environments/dev.tfvars

# Production
terraform apply -var-file=environments/prod.tfvars
```

### 3. Obter Endpoint

```bash
# Obter a URL base do API Gateway
terraform output api_gateway_endpoint

# Obter a URL completa com environment
terraform output api_gateway_url
```

## 📊 Outputs Disponíveis

Após o `terraform apply`, você terá acesso aos seguintes outputs:

```bash
# ID do API Gateway
terraform output api_gateway_id

# Endpoint base (sem environment)
terraform output api_gateway_endpoint

# URL completa com environment incluído
terraform output api_gateway_url

# URL de invocação do stage
terraform output api_gateway_invoke_url
```

## 🔒 Segurança

### CORS

Configurado para permitir:
- **Origins**: `*` (considere restringir em produção)
- **Methods**: GET, POST, PUT, PATCH, DELETE, OPTIONS
- **Headers**: `*`
- **Max Age**: 300 segundos

### Throttling

- **Burst Limit**: 5000 requisições
- **Rate Limit**: 2000 requisições por segundo

### VPC Link

Todas as requisições passam pelo VPC Link, garantindo:
- Comunicação privada com o NLB interno
- Isolamento da rede
- Acesso seguro aos serviços no EKS

## 🔄 Fluxo de Requisição

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │ 1. https://api.com/dev/auth/api/auth/login
       ▼
┌─────────────────────┐
│   API Gateway       │
│   (HTTP API)        │
└──────┬──────────────┘
       │ 2. Route match: /dev/auth/{proxy+}
       │    Extrai: /api/auth/login
       ▼
┌─────────────────────┐
│    VPC Link         │
│ (Conexão Privada)   │
└──────┬──────────────┘
       │ 3. Forward para NLB
       ▼
┌─────────────────────┐
│ Network Load        │
│    Balancer         │
└──────┬──────────────┘
       │ 4. Route para Ingress
       ▼
┌─────────────────────┐
│  NGINX Ingress      │
│   Controller        │
└──────┬──────────────┘
       │ 5. Match /auth -> rewrite
       ▼
┌─────────────────────┐
│   auth-service      │
│    Pod (8082)       │
└─────────────────────┘
```

## 🎯 Benefícios

1. **Isolamento por Environment**
   - URLs distintas para dev, staging, prod
   - Evita confusão entre ambientes
   - Permite testes independentes

2. **Versionamento**
   - Facilita migração entre versões
   - Permite manter múltiplas versões ativas

3. **Segurança**
   - Controle granular por environment
   - Facilita aplicação de políticas diferentes

4. **Monitoramento**
   - Métricas separadas por environment
   - Facilita troubleshooting

5. **Gestão**
   - Gerenciamento simplificado
   - Rollback facilitado
   - Configuração via IaC (Terraform)

## 📝 Notas

- O environment é automaticamente derivado do Terraform workspace
- As rotas são criadas dinamicamente com base no `local.environment`
- Cada serviço tem duas rotas: raiz e com proxy (`{proxy+}`)
- A integração usa `HTTP_PROXY` para encaminhar tudo ao NLB
- O Ingress Controller (NGINX) faz o roteamento final para os pods

## 🔧 Troubleshooting

### Rota não encontrada (404)

Verifique:
1. Se o environment no path está correto
2. Se a rota está registrada no API Gateway
3. Se o VPC Link está ativo
4. Se o NLB está saudável

### Timeout

Verifique:
1. Se o pod do serviço está rodando
2. Se o Ingress está configurado
3. Se o Security Group permite tráfego
4. Se o NLB está encaminhando para os nodes corretos

### Erro 500

Verifique:
1. Logs do API Gateway
2. Logs do NLB
3. Logs do Ingress Controller
4. Logs do pod do serviço
