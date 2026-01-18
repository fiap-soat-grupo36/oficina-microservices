# Oficina Microservices

Arquitetura de microserviços para sistema de gestão de oficina mecânica, deployada em AWS EKS com infraestrutura como código (Terraform) e gerenciamento de múltiplos ambientes via Kustomize.

## 📋 Arquitetura

Este projeto utiliza uma arquitetura de microserviços moderna com as seguintes características:

### Visão Geral da Arquitetura

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            CAMADA DE ACESSO                              │
│                                                                           │
│  Internet/VPN → AWS API Gateway (HTTP API) → VPC Link (Private)         │
│                           ↓                                               │
│                    AWS Network Load Balancer                             │
│                  (Balanceamento L4 - Porta 8761)                         │
└───────────────────────────────┬─────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                         AWS EKS CLUSTER                                  │
│                    (Kubernetes Gerenciado)                               │
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │              Namespace: oficina-mecanica-{env}                  │   │
│  │                                                                  │   │
│  │  ┌────────────────┐          ┌─────────────────────┐           │   │
│  │  │ Eureka Server  │ ←──────→ │  Metrics Server     │           │   │
│  │  │  (Port 8761)   │          │  (HPA Support)      │           │   │
│  │  │                │          └─────────────────────┘           │   │
│  │  │ • Service      │                                             │   │
│  │  │   Discovery    │          ┌─────────────────────┐           │   │
│  │  │ • Swagger      │          │   ConfigMaps &      │           │   │
│  │  │   Agregado     │ ←──────→ │   Secrets           │           │   │
│  │  └────────┬───────┘          │  (Terraform)        │           │   │
│  │           │                  └─────────────────────┘           │   │
│  │           │ Service Discovery via Feign Clients                │   │
│  │           ↓                                                     │   │
│  │  ┌────────────────────────────────────────────────────────┐   │   │
│  │  │              MICROSERVIÇOS BACKEND                      │   │   │
│  │  │                                                          │   │   │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────┐             │   │   │
│  │  │  │   Auth   │  │ Customer │  │ Catalog  │             │   │   │
│  │  │  │  (8082)  │  │  (8081)  │  │  (8083)  │             │   │   │
│  │  │  └─────┬────┘  └─────┬────┘  └─────┬────┘             │   │   │
│  │  │        │              │              │                  │   │   │
│  │  │  ┌─────┴──────┐ ┌────┴────┐  ┌─────┴────┐            │   │   │
│  │  │  │ Inventory  │ │ Budget  │  │   Work   │            │   │   │
│  │  │  │   (8084)   │ │ (8085)  │  │  Order   │            │   │   │
│  │  │  └────────────┘ └─────────┘  │  (8086)  │            │   │   │
│  │  │                               └─────┬────┘             │   │   │
│  │  │                                     │                  │   │   │
│  │  │                              ┌──────┴──────┐          │   │   │
│  │  │                              │Notification │          │   │   │
│  │  │                              │   (8087)    │          │   │   │
│  │  │                              └──────┬──────┘          │   │   │
│  │  └────────────────────────────────────┼──────────────────┘   │   │
│  │                                        │                      │   │
│  │  ┌─────────────────────────────────────┼──────────────────┐ │   │
│  │  │         HORIZONTAL POD AUTOSCALER (HPA)                │ │   │
│  │  │  • Min: 1-2 replicas (dev/prod)                        │ │   │
│  │  │  • Max: 2-5 replicas (dev/prod)                        │ │   │
│  │  │  • Target CPU: 70%                                     │ │   │
│  │  └──────────────────────────────────────────────────────┘ │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
└────────────────────────────────┬──────────────────────────────────────┘
                                 ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                        CAMADA DE DADOS                                   │
│                                                                           │
│  • AWS RDS PostgreSQL (Dev/Prod) ou                                     │
│  • PostgreSQL StatefulSet (Local)                                       │
│  • AWS Secrets Manager (Prod - recomendado)                             │
└───────────────────────────────────────────────────────────────────────────┘
```

### �🏗️ Infraestrutura AWS

A infraestrutura é provisionada automaticamente via **Terraform** e inclui:

#### Kubernetes (EKS)
- **EKS Cluster**: Cluster Kubernetes gerenciado pela AWS (assumido como pré-existente: `eks-fiap-oficina-mecanica`)
- **Namespaces isolados**: `oficina-mecanica-dev` e `oficina-mecanica-prod`
- **Metrics Server**: Auto-provisionado para suporte ao HPA (Horizontal Pod Autoscaler)
- **AWS NLB Controller**: Gerencia Network Load Balancers para expor serviços

#### Rede e Acesso
- **Network Load Balancer (NLB)**: Balanceador L4 por ambiente (dev/prod) expondo o Eureka Server na porta 8761
- **VPC Link**: Conecta o API Gateway ao NLB interno para acesso seguro aos microserviços
- **Security Groups**: Isolamento de rede entre componentes (VPC Link, NLB, EKS)
- **API Gateway (HTTP API)**: Ponto de entrada único via AWS API Gateway V2 (integração com NLB via VPC Link)

#### Dados e Configuração
- **RDS PostgreSQL** (assumido): Banco de dados gerenciado para ambientes dev/prod
- **ConfigMaps**: Configurações compartilhadas injetadas dinamicamente via Terraform
- **Secrets**: Gerenciados via Kubernetes Secrets (dev) e recomendado AWS Secrets Manager (prod)
- **Datadog Integration**: Observabilidade com API key e chaves de aplicação configuráveis

#### Deployment Automatizado
- **Kustomize**: Orquestração de manifestos Kubernetes com overlays por ambiente
- **Terraform Workspaces**: `dev` e `prod` isolados, cada um com seu próprio estado
- **Script de atualização**: `update_kustomize.sh` injeta tags de imagem dinamicamente durante o deploy

### 🎯 Organização dos Microserviços

#### Service Discovery - Netflix Eureka

Todos os microserviços se registram automaticamente no **Eureka Server**, permitindo comunicação dinâmica entre serviços sem URLs hardcoded. O Eureka também expõe um Swagger UI agregado que consolida a documentação de todos os microserviços.

**Dashboard do Eureka:** http://localhost:8761 (local) ou via API Gateway (cloud)

#### Serviços Backend

- **eureka-server** (porta 8761) - Service Registry e Swagger agregado
- **auth-service** (porta 8082) - Autenticação JWT e gerenciamento de usuários
- **customer-service** (porta 8081) - Gestão de clientes e veículos
- **catalog-service** (porta 8083) - Catálogo de serviços e produtos
- **inventory-service** (porta 8084) - Controle de estoque
- **budget-service** (porta 8085) - Gestão de orçamentos
- **work-order-service** (porta 8086) - Ordens de serviço e rastreamento
- **notification-service** (porta 8087) - Notificações assíncronas por email

#### Arquitetura de Comunicação

```
Internet → API Gateway → VPC Link → NLB (8761) → Eureka Server (K8s)
                                              ↓
                                         Service Mesh
                                              ↓
                      ┌───────────────────────┼───────────────────────┐
                      ↓                       ↓                       ↓
                 Auth Service          Customer Service       Catalog Service
                  (Port 8082)            (Port 8081)            (Port 8083)
                      ↓                       ↓                       ↓
                 Inventory Service      Budget Service      Work Order Service
                  (Port 8084)            (Port 8085)            (Port 8086)
                                              ↓
                                    Notification Service
                                        (Port 8087)
                                              ↓
                                      PostgreSQL (RDS/Pod)
```

Todos os serviços se comunicam via **Feign Clients** usando service discovery (nomes lógicos como `auth-service`, `customer-service`), que o Eureka resolve para IPs dinâmicos dos pods.

### 🔧 Kustomize: Gerenciamento Multi-Ambiente

O projeto usa **Kustomize overlays** para suportar 3 ambientes distintos:

#### Estrutura

```
k8s/
├── base/                    # Manifestos comuns (Deployments, Services, HPA)
│   ├── postgres.yaml        # StatefulSet PostgreSQL (apenas local)
│   ├── eureka-server.yaml   # Deployment + Service + NLB annotations
│   ├── *-service.yaml       # 7 microserviços (Deployment + Service)
│   ├── configmap-shared.yaml
│   └── hpa.yaml             # Horizontal Pod Autoscaler
│
└── overlays/
    ├── local/               # Minikube (namespace: oficina)
    │   └── Secrets hardcoded, Ingress local, PostgreSQL incluso
    │
    ├── dev/                 # EKS Dev (namespace: oficina-mecanica-dev)
    │   └── NLB interno, sem PostgreSQL (usa RDS), ConfigMap via Terraform
    │
    └── prod/                # EKS Prod (namespace: oficina-mecanica-prod)
        └── 2+ réplicas, mais recursos, NLB público, secrets externos
```

#### Diferenças Entre Ambientes

| Característica       | Local (Minikube)       | Dev (EKS)                 | Prod (EKS)                |
|---------------------|------------------------|---------------------------|---------------------------|
| **Namespace**       | `oficina`              | `oficina-mecanica-dev`    | `oficina-mecanica-prod`   |
| **PostgreSQL**      | Pod StatefulSet        | RDS (assumido)            | RDS (assumido)            |
| **NLB Scheme**      | N/A (Ingress local)    | `internal`                | `internet-facing`         |
| **Réplicas Base**   | 1                      | 1                         | 2                         |
| **HPA Min/Max**     | 1-2                    | 1-2                       | 2-5                       |
| **ConfigMap**       | Arquivo YAML           | Injetado via Terraform    | Injetado via Terraform    |
| **Secrets**         | Hardcoded (OK p/ dev)  | Kubernetes Secrets        | AWS Secrets Manager       |
| **Gerenciamento**   | `kubectl apply`        | Terraform (automático)    | Terraform (automático)    |

#### Como o Kustomize é Aplicado

**Local (Manual):**
```bash
kubectl apply -k k8s/overlays/local
```

**Dev/Prod (Terraform Automatizado):**
1. Terraform executa `scripts/update_kustomize.sh` com a tag da imagem desejada
2. Script atualiza `kustomization.yaml` com as novas tags usando `kustomize edit set image`
3. Script executa `kubectl kustomize` e retorna os manifestos finais em base64
4. Terraform aplica os manifestos via `kubectl_manifest` com **server-side apply** (evita conflitos com HPA)

### 🌐 API Gateway: Roteamento Centralizado

#### Fluxo de Requisições

```
Cliente → API Gateway (AWS) → VPC Link → NLB → Eureka Service (K8s) → Backend Services
```

#### Mapeamento de Rotas (Planejado)

| Prefixo        | Serviço Backend       | Porta | Descrição                     |
|----------------|----------------------|-------|-------------------------------|
| `/eureka`      | eureka-server        | 8761  | Service Discovery & Swagger   |
| `/auth`        | auth-service         | 8082  | Autenticação JWT              |
| `/customer`    | customer-service     | 8081  | Clientes & Veículos           |
| `/catalog`     | catalog-service      | 8083  | Catálogo de Produtos          |
| `/inventory`   | inventory-service    | 8084  | Controle de Estoque           |
| `/budget`      | budget-service       | 8085  | Orçamentos                    |
| `/work-order`  | work-order-service   | 8086  | Ordens de Serviço             |
| `/notification`| notification-service | 8087  | Notificações                  |

**Exemplo de URL:**
```
https://api.oficina-mecanica.com/auth/api/auth/login
```

#### Componentes do Roteamento

1. **AWS API Gateway (HTTP API)**: Ponto de entrada público/privado
2. **VPC Link**: Conecta API Gateway à rede privada do EKS
3. **Network Load Balancer**: Balanceador L4 expondo porta 8761 (Eureka)
4. **Kubernetes Service (NLB)**: Service type `LoadBalancer` com annotations AWS
5. **Eureka Server**: Roteia internamente para os microserviços via service discovery

---

## 🏠 Desenvolvimento Local - Guia Completo

Este guia detalha **3 formas de rodar o projeto localmente** para desenvolvimento. Escolha a que melhor se adequa ao seu cenário.

### 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- ✅ **Java 21** - [Download](https://adoptium.net/)
- ✅ **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- ✅ **Docker & Docker Compose** - [Download](https://www.docker.com/products/docker-desktop)
- ✅ **Git** - Para clonar o repositório

**Verificar instalação:**
```bash
java -version    # Deve mostrar Java 21
mvn -version     # Deve mostrar Maven 3.9+
docker --version # Deve mostrar Docker 20.10+
docker compose version
```

---

### 🎯 Opção 1: Docker Compose (⭐ Recomendado)

**Vantagens:** Rápido, isolado, não precisa configurar banco manualmente, simula ambiente de produção.

#### Passo a Passo

**1. Clonar o repositório**
```bash
git clone https://github.com/seu-usuario/oficina-microservices.git
cd oficina-microservices
```

**2. Subir todos os serviços**
```bash
# Sobe PostgreSQL + Eureka + todos os 7 microserviços
docker compose --profile dev up -d

# Acompanhar os logs (Ctrl+C para sair)
docker compose --profile dev logs -f
```

**3. Aguardar inicialização (⏱️ ~2-3 minutos)**

O Docker Compose inicia os serviços na ordem correta:
1. PostgreSQL (porta 5432)
2. Eureka Server (porta 8761)
3. Auth Service (porta 8082)
4. Demais microserviços (portas 8081-8087)

**4. Verificar se tudo está funcionando**

```bash
# Ver status de todos os containers
docker compose --profile dev ps

# Todos devem estar "healthy" ou "running"
# Se algum estiver "unhealthy", veja os logs:
docker compose --profile dev logs auth-service
```

**5. Acessar os serviços**

🌐 **Eureka Dashboard (Service Registry):**
- URL: http://localhost:8761
- Aguarde até ver todos os 7 serviços registrados

📖 **Swagger Agregado (Todas as APIs em um lugar):**
- URL: http://localhost:8761/swagger-ui.html
- Use o dropdown para selecionar cada serviço

**6. Testar uma chamada (exemplo)**

```bash
# 1. Criar um usuário
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@example.com",
    "senha": "senha123",
    "role": "MECANICO"
  }'

# 2. Fazer login
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "senha": "senha123"
  }'

# Copie o token JWT retornado para usar nas próximas chamadas
```

**7. Parar tudo quando terminar**

```bash
# Parar mas manter os dados
docker compose --profile dev stop

# Parar e remover containers (mantém volumes/dados)
docker compose --profile dev down

# Parar e LIMPAR TUDO (incluindo banco de dados)
docker compose --profile dev down -v
```

#### 🔧 Comandos Úteis - Docker Compose

```bash
# Ver logs de um serviço específico
docker compose --profile dev logs -f customer-service

# Reiniciar um serviço específico
docker compose --profile dev restart auth-service

# Rebuild após mudanças no código
docker compose --profile dev up -d --build

# Ver uso de recursos
docker stats

# Acessar terminal de um container
docker exec -it customer-service bash
```

---

### 🎯 Opção 2: Minikube (Kubernetes Local)

**Vantagens:** Testa deploy em Kubernetes, mais próximo do ambiente de produção.

#### Passo a Passo

**1. Instalar Minikube**
```bash
# macOS
brew install minikube

# Linux
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Verificar
minikube version
```

**2. Iniciar Minikube**
```bash
# Subir cluster local com recursos adequados
minikube start --cpus=4 --memory=8192 --driver=docker

# Habilitar addons necessários
minikube addons enable ingress
minikube addons enable metrics-server
```

**3. Build das imagens localmente (usar Docker do Minikube)**

```bash
# Configurar shell para usar Docker do Minikube
eval $(minikube docker-env)

# Build de todas as imagens
cd oficina-microservices
make build TAG=latest

# Ou build manual
mvn clean install -DskipTests
docker build -t grecomilani/oficina-eureka-server:latest -f eureka-server/Dockerfile .
docker build -t grecomilani/oficina-auth-service:latest -f auth-service/Dockerfile .
# ... repetir para todos os serviços
```

**4. Aplicar manifestos Kubernetes**

```bash
# Usar o overlay local (namespace: oficina)
kubectl apply -k k8s/overlays/local

# Acompanhar os pods subindo
kubectl -n oficina get pods -w
```

**5. Aguardar todos os pods ficarem Running (⏱️ ~3-5 minutos)**

```bash
# Verificar status
kubectl -n oficina get pods

# Todos devem estar 1/1 Running
# Se algum estiver CrashLoopBackOff:
kubectl -n oficina logs -f pod/<nome-do-pod>
```

**6. Acessar os serviços**

**Opção A: Port-Forward (Recomendado para dev)**
```bash
# Eureka Dashboard
kubectl -n oficina port-forward svc/eureka-server 8761:8761

# Auth Service
kubectl -n oficina port-forward svc/auth-service 8082:8082

# Customer Service
kubectl -n oficina port-forward svc/customer-service 8081:8081

# Acesse: http://localhost:8761
```

**Opção B: Ingress (acesso via domínio)**
```bash
# Adicionar ao /etc/hosts
echo "$(minikube ip) oficina.local" | sudo tee -a /etc/hosts

# Acessar via:
# http://oficina.local/eureka
# http://oficina.local/auth
# http://oficina.local/customer
```

**7. Limpar tudo**

```bash
# Deletar todos os recursos
kubectl delete -k k8s/overlays/local

# Parar Minikube
minikube stop

# Deletar cluster completamente
minikube delete
```

---

### 🎯 Opção 3: Maven Local (Sem Containers)

**Vantagens:** Útil para debug, desenvolvimento isolado de um serviço, não precisa de Docker.

**⚠️ Atenção:** Você precisará de um PostgreSQL rodando (pode usar Docker apenas para o banco).

#### Passo a Passo

**1. Subir PostgreSQL (via Docker)**
```bash
docker run -d \
  --name postgres-oficina \
  -e POSTGRES_DB=oficina-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine

# Verificar se está rodando
docker ps | grep postgres-oficina
```

**2. Build do projeto**
```bash
cd oficina-microservices

# Compilar todos os módulos (necessário por causa da shared-library)
mvn clean install -DskipTests
```

**3. Iniciar Eureka Server (SEMPRE primeiro!)**
```bash
cd eureka-server
mvn spring-boot:run

# Aguarde ver a mensagem:
# "Started EurekaServerApplication in X seconds"

# Acesse: http://localhost:8761
```

**4. Iniciar serviços (cada um em um terminal separado)**

```bash
# Terminal 2 - Auth Service
cd auth-service
mvn spring-boot:run

# Terminal 3 - Customer Service
cd customer-service
mvn spring-boot:run

# Terminal 4 - Catalog Service
cd catalog-service
mvn spring-boot:run

# Terminal 5 - Inventory Service
cd inventory-service
mvn spring-boot:run

# Terminal 6 - Budget Service
cd budget-service
mvn spring-boot:run

# Terminal 7 - Work Order Service
cd work-order-service
mvn spring-boot:run

# Terminal 8 - Notification Service
cd notification-service
mvn spring-boot:run
```

**Dica:** Use **tmux** ou **screen** para gerenciar múltiplos terminais:
```bash
# Instalar tmux
brew install tmux  # macOS
sudo apt install tmux  # Linux

# Criar sessão tmux
tmux new -s oficina

# Dividir em painéis: Ctrl+b então "
# Navegar entre painéis: Ctrl+b então seta
```

**5. Verificar serviços no Eureka**

Acesse http://localhost:8761 e confirme que todos os 7 serviços aparecem na lista "Instances currently registered with Eureka".

**6. Parar tudo**

```bash
# Parar cada terminal com Ctrl+C

# Parar PostgreSQL
docker stop postgres-oficina
docker rm postgres-oficina
```

---

## ✅ Checklist de Verificação

Após subir o ambiente (qualquer opção), verifique:

- [ ] ✅ **Eureka Dashboard** (http://localhost:8761) mostra 7-8 serviços registrados
- [ ] ✅ **Swagger Agregado** (http://localhost:8761/swagger-ui.html) abre corretamente
- [ ] ✅ **Health checks** funcionando:
  ```bash
  curl http://localhost:8761/actuator/health  # Eureka
  curl http://localhost:8082/actuator/health  # Auth
  curl http://localhost:8081/actuator/health  # Customer
  ```
- [ ] ✅ **PostgreSQL** está acessível (porta 5432)
- [ ] ✅ **Sem erros** nos logs dos serviços

---

## 🌐 Endpoints Importantes (Local)

| Serviço | Porta | Swagger | Actuator Health |
|---------|-------|---------|-----------------|
| **Eureka Server** | 8761 | http://localhost:8761/swagger-ui.html | http://localhost:8761/actuator/health |
| **Auth Service** | 8082 | http://localhost:8082/swagger-ui.html | http://localhost:8082/actuator/health |
| **Customer Service** | 8081 | http://localhost:8081/swagger-ui.html | http://localhost:8081/actuator/health |
| **Catalog Service** | 8083 | http://localhost:8083/swagger-ui.html | http://localhost:8083/actuator/health |
| **Inventory Service** | 8084 | http://localhost:8084/swagger-ui.html | http://localhost:8084/actuator/health |
| **Budget Service** | 8085 | http://localhost:8085/swagger-ui.html | http://localhost:8085/actuator/health |
| **Work Order Service** | 8086 | http://localhost:8086/swagger-ui.html | http://localhost:8086/actuator/health |
| **Notification Service** | 8087 | - | http://localhost:8087/actuator/health |

---

## 🐛 Troubleshooting - Problemas Comuns em Local

### Serviço não registra no Eureka

**Sintomas:** Serviço sobe mas não aparece em http://localhost:8761

**Soluções:**
```bash
# 1. Verifique se o Eureka está rodando
curl http://localhost:8761/actuator/health

# 2. Aguarde 30 segundos (delay normal de registro)

# 3. Veja os logs do serviço
docker compose --profile dev logs auth-service | grep -i eureka

# 4. Verifique se a porta do Eureka está correta
# Deve estar acessível em localhost:8761
```

### Porta já em uso

**Sintomas:** Erro "Address already in use" ou "bind: address already in use"

**Soluções:**
```bash
# Ver o que está usando a porta
lsof -i :8761  # Substitua pelo número da porta
netstat -tulpn | grep 8761

# Matar o processo
kill -9 <PID>

# Ou mudar a porta no application.yml do serviço
```

### Banco de dados não conecta

**Sintomas:** Erros de "Connection refused" ou "Could not connect to database"

**Soluções:**
```bash
# 1. Verificar se PostgreSQL está rodando
docker ps | grep postgres

# 2. Testar conexão diretamente
docker exec -it postgres psql -U postgres -d oficina-db

# 3. Verificar configuração no application.yml
# URL deve ser: jdbc:postgresql://localhost:5432/oficina-db
# Para Docker Compose: jdbc:postgresql://postgres:5432/oficina-db
```

### Serviços não se comunicam (Feign errors)

**Sintomas:** Erros 404 ou "Load balancer does not have available server"

**Soluções:**
```bash
# 1. Todos os serviços devem estar no Eureka
curl http://localhost:8761/eureka/apps

# 2. Aguarde 30-60 segundos após todos subirem

# 3. Verifique se os nomes dos serviços estão corretos
# Devem ser exatamente: auth-service, customer-service, etc.

# 4. Veja logs de Feign no serviço
docker compose --profile dev logs work-order-service | grep -i feign
```

### Build do Maven falha

**Sintomas:** Erros de compilação, testes falhando, dependências não encontradas

**Soluções:**
```bash
# 1. Limpar cache do Maven
mvn clean

# 2. Rebuild completo (do diretório raiz!)
cd oficina-microservices
mvn clean install -DskipTests

# 3. Se shared-library não for encontrada
cd shared-library
mvn clean install
cd ..

# 4. Limpar cache local se necessário
rm -rf ~/.m2/repository/br/com/fiap/oficina
mvn clean install
```

### Containers ficam "unhealthy"

**Sintomas:** `docker compose ps` mostra status "unhealthy"

**Soluções:**
```bash
# 1. Ver logs do container
docker compose --profile dev logs <service-name>

# 2. Verificar health check manualmente
docker exec <container-name> wget -qO- http://localhost:8082/actuator/health

# 3. Aumentar tempo de inicialização
# Edite docker-compose.yml e aumente start_period no healthcheck

# 4. Rebuild a imagem
docker compose --profile dev up -d --build <service-name>
```

---

## 🎓 Próximos Passos Após Rodar Local

1. 📖 **Explore a API** via Swagger: http://localhost:8761/swagger-ui.html
2. 🧪 **Rode os testes**: `mvn test`
3. 📊 **Monitore no Eureka**: http://localhost:8761
4. 🔍 **Veja o CLAUDE.md** na raiz para entender a arquitetura completa
5. 🚀 **Deploy em Dev/Prod**: Veja `k8s/README-OVERLAYS.md`

---

---

## 🚀 Deploy em Produção (AWS EKS)

### Pré-requisitos AWS

- AWS CLI configurado (`aws configure`)
- Terraform >= 1.5
- EKS Cluster pré-existente (`eks-fiap-oficina-mecanica`)
- RDS PostgreSQL configurado (ou usar PostgreSQL no Kubernetes)
- Imagens Docker publicadas no Docker Hub

### Deploy via Terraform

```bash
cd infra

# 1. Inicializar Terraform (primeira vez)
terraform init

# 2. Selecionar workspace (dev ou prod)
terraform workspace select dev
# ou
terraform workspace new prod
terraform workspace select prod

# 3. Planejar mudanças
terraform plan -var-file=environments/dev.tfvars

# 4. Aplicar infraestrutura
terraform apply -var-file=environments/dev.tfvars

# O Terraform irá:
# ✓ Criar namespace (oficina-mecanica-dev ou oficina-mecanica-prod)
# ✓ Criar ConfigMaps com URLs de banco e Eureka
# ✓ Criar Secrets (JWT, database, notification)
# ✓ Provisionar NLB para expor Eureka (porta 8761)
# ✓ Criar VPC Link para integração com API Gateway
# ✓ Aplicar Kustomize overlays (todos os deployments e services)
# ✓ Instalar Metrics Server (apenas dev)
# ✓ Configurar AWS NLB Controller

# 5. Verificar recursos criados
kubectl config use-context <contexto-do-eks>
kubectl -n oficina-mecanica-dev get pods
kubectl -n oficina-mecanica-dev get svc

# 6. Obter URL do Load Balancer
terraform output nlb_dns_name
# ou
kubectl -n oficina-mecanica-dev get svc eureka-server-nlb
```

### Atualizar Versão das Imagens

```bash
cd infra

# Aplicar com nova tag
terraform apply -var="image_tag=v2.0.0" -var-file=environments/dev.tfvars

# O script update_kustomize.sh será executado automaticamente
# e atualizará todas as imagens para a nova tag
```

### Destruir Infraestrutura

```bash
cd infra
terraform workspace select dev
terraform destroy -var-file=environments/dev.tfvars
```

### 🔑 Comandos Terraform Úteis

```bash
# Listar workspaces
terraform workspace list

# Ver estado atual
terraform show

# Ver outputs (URLs, ARNs, etc.)
terraform output
terraform output nlb_dns_name
terraform output vpc_link_id

# Validar configuração
terraform validate

# Formatar código
terraform fmt -recursive

# Ver plano detalhado
terraform plan -var-file=environments/dev.tfvars -out=tfplan
terraform show tfplan

# Aplicar plano salvo
terraform apply tfplan

# Refresh state (sincronizar com recursos reais)
terraform refresh -var-file=environments/dev.tfvars

# Listar recursos no state
terraform state list

# Ver detalhes de um recurso
terraform state show kubectl_manifest.kustomization[\"v1/Namespace/oficina-mecanica-dev\"]

# Remover recurso do state (sem deletar o recurso real)
terraform state rm kubernetes_namespace.oficina

# Importar recurso existente
terraform import kubernetes_namespace.oficina oficina-mecanica-dev

# Unlock state se travado
terraform force-unlock <LOCK_ID>
```

### 🔧 Comandos Kustomize Úteis

```bash
# Ver manifests finais sem aplicar (dry-run)
kubectl kustomize k8s/overlays/dev
kubectl kustomize k8s/overlays/prod > preview.yaml

# Validar sintaxe
kubectl kustomize k8s/overlays/dev --enable-helm

# Comparar diferenças entre ambientes
diff <(kubectl kustomize k8s/overlays/dev) <(kubectl kustomize k8s/overlays/prod)

# Atualizar imagens manualmente
cd k8s/overlays/dev
kustomize edit set image \
  grecomilani/oficina-eureka-server=grecomilani/oficina-eureka-server:v2.0.0 \
  grecomilani/oficina-auth-service=grecomilani/oficina-auth-service:v2.0.0
  
# Verificar versão atual das imagens
grep -r "newName:" k8s/overlays/dev/kustomization.yaml
```

### 🚀 Comandos Kubernetes Úteis

```bash
# Contexto e namespace
kubectl config current-context
kubectl config use-context <context-name>
kubectl config set-context --current --namespace=oficina-mecanica-dev

# Pods e logs
kubectl -n oficina-mecanica-dev get pods -o wide
kubectl -n oficina-mecanica-dev logs -f <pod-name>
kubectl -n oficina-mecanica-dev logs -f deployment/auth-service
kubectl -n oficina-mecanica-dev logs -f <pod-name> --previous  # logs do pod anterior (crash)

# Executar comandos em pods
kubectl -n oficina-mecanica-dev exec -it <pod-name> -- /bin/sh
kubectl -n oficina-mecanica-dev exec -it <pod-name> -- curl http://localhost:8082/actuator/health

# Deployments
kubectl -n oficina-mecanica-dev get deployments
kubectl -n oficina-mecanica-dev describe deployment auth-service
kubectl -n oficina-mecanica-dev rollout status deployment/auth-service
kubectl -n oficina-mecanica-dev rollout restart deployment/auth-service
kubectl -n oficina-mecanica-dev rollout undo deployment/auth-service  # rollback

# Services e endpoints
kubectl -n oficina-mecanica-dev get svc
kubectl -n oficina-mecanica-dev get endpoints
kubectl -n oficina-mecanica-dev describe svc eureka-server-nlb

# HPA (Horizontal Pod Autoscaler)
kubectl -n oficina-mecanica-dev get hpa
kubectl -n oficina-mecanica-dev describe hpa auth-service-hpa
kubectl -n oficina-mecanica-dev top pods  # uso de CPU/memória

# ConfigMaps e Secrets
kubectl -n oficina-mecanica-dev get configmaps
kubectl -n oficina-mecanica-dev describe configmap oficina-shared-config
kubectl -n oficina-mecanica-dev get secrets
kubectl -n oficina-mecanica-dev get secret auth-jwt-secret -o jsonpath='{.data.JWT_SECRET}' | base64 -d

# Events (debug de problemas)
kubectl -n oficina-mecanica-dev get events --sort-by='.lastTimestamp'
kubectl -n oficina-mecanica-dev get events --field-selector type=Warning

# Describe para troubleshooting
kubectl -n oficina-mecanica-dev describe pod <pod-name>
kubectl -n oficina-mecanica-dev describe node <node-name>

# Delete recursos
kubectl -n oficina-mecanica-dev delete pod <pod-name>  # recria automaticamente
kubectl -n oficina-mecanica-dev delete deployment auth-service
kubectl delete -k k8s/overlays/dev  # deleta todo o overlay

# Port-forward para debug
kubectl -n oficina-mecanica-dev port-forward svc/eureka-server 8761:8761
kubectl -n oficina-mecanica-dev port-forward deployment/auth-service 8082:8082
```

---

## 🚀 Outras Formas de Executar

### Pré-requisitos

- Java 21
- Maven 3.9+
- Docker e Docker Compose

### Ordem de Inicialização

É importante iniciar os serviços na ordem correta:

1. **Eureka Server** (primeiro)
2. **Demais microserviços**

### Executar com Docker Compose

```bash
# Dev: build local e sobe todos os serviços
docker compose --profile dev up -d

# Prod: usa imagens publicadas (defina REGISTRY/TAG)
REGISTRY=seu-usuario TAG=latest docker compose --profile prod -f docker-compose.yml -f docker-compose.prod.yml up -d

# Verificar o status dos serviços
docker compose --profile dev ps

# Visualizar logs
docker compose --profile dev logs -f

# Parar todos os serviços
docker compose --profile dev down
docker compose --profile prod -f docker-compose.yml -f docker-compose.prod.yml down
```

### Executar Localmente (para desenvolvimento)

```bash
# 1. Compilar todos os módulos
mvn clean install -DskipTests

# 2. Iniciar o Eureka Server primeiro
cd eureka-server
mvn spring-boot:run

# 3. Em outros terminais, iniciar os demais serviços
cd auth-service
mvn spring-boot:run

cd customer-service
mvn spring-boot:run

# ... e assim por diante
```

### Publicar imagens no Docker Hub com Makefile

Use o `Makefile` da raiz para padronizar build/push das imagens (cada dev usa seu próprio Docker Hub):

```bash
# 1) Faça login no seu Docker Hub
docker login

# 2) Build de todas as imagens (tag padrão: latest)
make build TAG=latest

# 3) Build + push para seu namespace (troque pelo seu usuário)
make push REGISTRY=seu-usuario TAG=latest

# 4) Multi-arch (amd64+arm64) com buildx e push
make buildx-push REGISTRY=seu-usuario TAG=latest PLATFORMS='linux/amd64,linux/arm64'

# 5) Limpar imagens locais geradas
make clean TAG=latest
```

Dicas rápidas:
- `REGISTRY` é obrigatório para push (ex.: `REGISTRY=grecomilani`).
- Ajuste `TAG` conforme a versão que quiser publicar (ex.: `v1.0.0`).
- Para multi-arch, garanta um builder ativo: `docker buildx create --name multi --use` (uma vez só).
- Se usar tags publicadas no Kubernetes, atualize os `image:` em `k8s/*.yaml` para `seu-usuario/oficina-<serviço>:<tag>`.

### Rodar no Minikube (k8s/)

```bash
# 1) Subir o Minikube com recursos mínimos
minikube start --cpus=4 --memory=8192 --kubernetes-version=v1.26.0

# Add-ons úteis (antes do apply, uma vez só)
minikube addons enable metrics-server
minikube addons enable dashboard
# Se quiser ingress (roteamento HTTP único):
minikube addons enable ingress
# Adicione ao /etc/hosts: \"$(minikube ip) oficina.local\"
# Ingress disponível em http://oficina.local/<path> (ver caminhos abaixo)
# Abrir dashboard (faz proxy e abre no navegador)
minikube dashboard

# 2) Disponibilizar imagens no cluster:
#    2a) Se já publicou no Docker Hub (ex.: grecomilani), pule para o passo 3
#    2b) Ou carregue no daemon do Minikube:
eval "$(minikube docker-env)"
make build TAG=latest
for img in grecomilani/oficina-{eureka-server,auth-service,customer-service,catalog-service,inventory-service,budget-service,work-order-service,notification-service}:latest; do
  minikube image load "$img"
done

# 3) Aplicar manifests (kustomize já cuida da ordem)
kubectl apply -k k8s

# 4) Acompanhar os pods
kubectl -n oficina get pods -w

# 5) Port-forward para acessar (um terminal por serviço ou use tmux)
kubectl -n oficina port-forward svc/eureka-server 8761:8761           # Eureka / Swagger agregado
kubectl -n oficina port-forward svc/auth-service 8082:8082            # Auth
kubectl -n oficina port-forward svc/customer-service 8081:8081        # Customer
kubectl -n oficina port-forward svc/catalog-service 8083:8083         # Catalog
kubectl -n oficina port-forward svc/inventory-service 8084:8084       # Inventory
kubectl -n oficina port-forward svc/budget-service 8085:8085          # Budget
kubectl -n oficina port-forward svc/work-order-service 8086:8086      # Work Order
kubectl -n oficina port-forward svc/notification-service 8087:8087    # Notification
kubectl -n oficina port-forward svc/postgres 5432:5432                # Postgres (para DBeaver)

# 6) Opcional: ajustar StorageClass se sua classe não for "standard"
#    (editar k8s/postgres.yaml antes do apply)

# Add-ons úteis (antes do apply, uma vez só)
minikube addons enable metrics-server
minikube addons enable dashboard
# Se quiser ingress (roteamento HTTP único):
minikube addons enable ingress
# Adicione ao /etc/hosts: \"$(minikube ip) oficina.local\"
# Ingress disponível em http://oficina.local/<path> (ver caminhos abaixo)
# Abrir dashboard (faz proxy e abre no navegador)
minikube dashboard
```

Add-ons em uso:
- `metrics-server`: coleta métricas para o HPA (`k8s/hpa.yaml`).
- `dashboard`: UI web para inspecionar recursos (comando `minikube dashboard`).
- `ingress`: controller ingress-nginx para expor tudo via host `oficina.local`.

Ingress (k8s/ingress.yaml) expõe tudo em um host único (`oficina.local`) com paths:
- `/eureka` → eureka-server (8761)
- `/auth` → auth-service (8082)
- `/customer` → customer-service (8081)
- `/catalog` → catalog-service (8083)
- `/inventory` → inventory-service (8084)
- `/budget` → budget-service (8085)
- `/work-order` → work-order-service (8086)
- `/notification` → notification-service (8087)

Para limpar tudo (nuke):
```bash
kubectl delete -k k8s || true
minikube stop
minikube delete
```

## 📊 Monitoramento

### Eureka Dashboard

Acesse http://localhost:8761 para visualizar:
- Todos os serviços registrados
- Status de cada instância
- Metadata dos serviços

### Actuator Endpoints

Todos os serviços expõem endpoints de monitoramento:
- `/actuator/health` - Status de saúde do serviço
- `/actuator/info` - Informações do serviço
- `/actuator/metrics` - Métricas da aplicação

## 🔧 Tecnologias

### Backend & Framework
- **Spring Boot 3.5.3** - Framework principal
- **Spring Cloud 2025.0.0** - Cloud native patterns
- **Netflix Eureka** - Service Discovery
- **OpenFeign** - Comunicação entre microserviços
- **Java 21** - Linguagem de programação
- **Maven** - Gerenciamento de dependências

### Infraestrutura & Cloud
- **AWS EKS** - Kubernetes gerenciado
- **AWS NLB** - Network Load Balancer (L4)
- **AWS API Gateway V2** - HTTP API para roteamento centralizado
- **AWS VPC Link** - Integração privada API Gateway ↔ VPC
- **Terraform** - Infrastructure as Code (IaC)
- **Kustomize** - Gerenciamento de manifestos Kubernetes
- **Docker** - Containerização

### Dados & Persistência
- **PostgreSQL** - Banco de dados relacional (RDS ou pod StatefulSet)
- **H2** - Banco de dados em memória (testes)
- **Flyway** (planejado) - Migrations de banco

### Observabilidade
- **Spring Boot Actuator** - Endpoints de health, metrics, info
- **Datadog** (opcional) - APM e logs centralizados
- **Kubernetes Metrics Server** - Métricas para HPA

### CI/CD
- **GitHub Actions** (planejado) - Pipelines de build e deploy
- **Docker Hub** - Registry de imagens
- **Makefile** - Automação de build multi-arch (amd64/arm64)

## 📚 Documentação da API

### Swagger Agregado

Para facilitar o acesso à documentação de todos os microserviços em um único local, o **Eureka Server** disponibiliza um Swagger agregado:

**🔗 Acesso único:** http://localhost:8761/swagger-ui.html (local) ou via API Gateway (cloud)

Através do Swagger agregado, você pode visualizar e testar as APIs de todos os serviços através de um dropdown, sem precisar acessar cada serviço individualmente.

#### Como funciona

- **URLs de documentação**: O Swagger agregado usa `localhost` nas URLs para que o navegador possa acessá-las
- **Comunicação entre serviços**: A comunicação interna via Feign continua usando nomes de serviços (ex: `auth-service`)
- **CORS**: Configuração CORS global na `shared-library` permite acesso cross-origin aos endpoints de documentação

### Documentação Individual dos Serviços

Cada serviço também expõe sua documentação OpenAPI de forma independente:

- Auth Service: http://localhost:8082/swagger-ui.html
- Customer Service: http://localhost:8081/swagger-ui.html
- Catalog Service: http://localhost:8083/swagger-ui.html
- Inventory Service: http://localhost:8084/swagger-ui.html
- Budget Service: http://localhost:8085/swagger-ui.html
- Work Order Service: http://localhost:8086/swagger-ui.html

### 📖 Documentação Técnica Adicional

Para informações detalhadas sobre aspectos específicos da arquitetura, consulte:

- **[ROUTING-ARCHITECTURE.md](docs/ROUTING-ARCHITECTURE.md)**: Fluxo completo de roteamento (API Gateway → VPC Link → NLB → Kubernetes), padrões de path, rewrite rules do NGINX Ingress
- **[KUBERNETES-IMAGE-UPDATE-STRATEGY.md](docs/KUBERNETES-IMAGE-UPDATE-STRATEGY.md)**: Estratégias para atualização de imagens Docker no Kubernetes
- **[SEED-DATA-AND-API-TESTS.md](docs/SEED-DATA-AND-API-TESTS.md)**: Dados de seed para testes e exemplos de chamadas de API
- **[SWAGGER-AGGREGATION.md](docs/SWAGGER-AGGREGATION.md)**: Implementação do Swagger agregado no Eureka Server
- **[k8s/README.md](k8s/README.md)**: Guia básico de deploy Kubernetes
- **[k8s/README-OVERLAYS.md](k8s/README-OVERLAYS.md)**: Detalhes sobre Kustomize overlays e diferenças entre ambientes
- **[AGENTS.md](AGENTS.md)**: Diretrizes para desenvolvimento e contribuição (estrutura, build, testes, commits)

## 🔍 Troubleshooting

### Serviços não aparecem no Eureka

1. Verifique se o Eureka Server está rodando
2. Aguarde até 30 segundos - o registro pode levar alguns segundos
3. Verifique os logs do serviço para erros de conexão

### Erro de comunicação entre serviços

1. Certifique-se de que todos os serviços estão registrados no Eureka
2. Verifique se a variável de ambiente `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` está configurada corretamente
3. Confirme que os nomes dos serviços nos FeignClients correspondem aos nomes registrados no Eureka

### Problemas com Docker

```bash
# Reconstruir imagens (dev)
docker compose --profile dev up -d --build

# Limpar volumes
docker compose --profile dev down -v

# Reiniciar tudo
docker compose --profile dev up -d --force-recreate
```

### Problemas com Terraform

**Erro: "No changes detected" mas recursos não foram criados**
```bash
# Verificar se o workspace está correto
terraform workspace list
terraform workspace select dev

# Forçar re-aplicação
terraform taint kubectl_manifest.kustomization
terraform apply -var-file=environments/dev.tfvars
```

**Erro: "Resource already exists" ou conflitos de ownership**
```bash
# Importar recursos existentes
terraform import kubernetes_namespace.oficina oficina-mecanica-dev

# Ou usar server-side apply (já configurado em k8s.tf)
# O parâmetro force_conflicts=true sobrescreve campos gerenciados por outros controllers
```

**Erro: "Script update_kustomize.sh failed"**
```bash
# Verificar permissões do script
chmod +x infra/scripts/update_kustomize.sh

# Testar manualmente
cd k8s/overlays/dev
kustomize edit set image grecomilani/oficina-eureka-server=grecomilani/oficina-eureka-server:latest
kubectl kustomize .
```

**Erro: VPC Link ou NLB não provisiona**
```bash
# Verificar se o EKS cluster existe
aws eks describe-cluster --name eks-fiap-oficina-mecanica --region us-east-2

# Verificar subnets e security groups
terraform console
> data.aws_eks_cluster.oficina.vpc_config[0].subnet_ids
> data.aws_vpc.main.id
```

### Problemas com Kustomize

**Erro: "Resource not found" ao aplicar overlays**
```bash
# Verificar se os recursos base existem
kubectl kustomize k8s/base

# Verificar se o overlay está correto
kubectl kustomize k8s/overlays/dev

# Aplicar base primeiro (debug)
kubectl apply -k k8s/base
```

**Imagens não atualizam após deploy**
```bash
# Verificar se imagePullPolicy está configurado
kubectl -n oficina-mecanica-dev get deploy auth-service -o jsonpath='{.spec.template.spec.containers[0].imagePullPolicy}'

# Deve retornar "Always" para dev
# Forçar pull da imagem
kubectl -n oficina-mecanica-dev rollout restart deployment auth-service
```

**HPA não escala pods**
```bash
# Verificar se Metrics Server está rodando
kubectl -n kube-system get pods -l k8s-app=metrics-server

# Verificar métricas dos pods
kubectl -n oficina-mecanica-dev top pods

# Verificar configuração do HPA
kubectl -n oficina-mecanica-dev get hpa
kubectl -n oficina-mecanica-dev describe hpa auth-service-hpa
```

### Problemas com NLB e API Gateway

**NLB não roteia tráfego para pods**
```bash
# Verificar targets registrados no Target Group
aws elbv2 describe-target-health \
  --target-group-arn $(terraform output -raw target_group_arn)

# Verificar Service no Kubernetes
kubectl -n oficina-mecanica-dev get svc eureka-server-nlb
kubectl -n oficina-mecanica-dev describe svc eureka-server-nlb

# Verificar annotations do NLB
kubectl -n oficina-mecanica-dev get svc eureka-server-nlb -o yaml | grep annotations -A 10
```

**API Gateway retorna 503 ou timeout**
```bash
# Verificar VPC Link status
aws apigatewayv2 get-vpc-link --vpc-link-id $(terraform output -raw vpc_link_id)
# Status deve ser "AVAILABLE"

# Verificar security groups
aws ec2 describe-security-groups --group-ids $(terraform output -raw vpc_link_security_group_id)

# Testar NLB diretamente (bypass API Gateway)
NLB_DNS=$(terraform output -raw nlb_dns_name)
curl http://${NLB_DNS}:8761/actuator/health
```

## 📝 Estrutura do Projeto

```
oficina-microservices/
├── infra/                       # Infraestrutura como Código (Terraform)
│   ├── providers.tf             # AWS, Kubernetes, Kubectl, Helm providers
│   ├── k8s.tf                   # Deploy Kustomize + Metrics Server
│   ├── nlb.tf                   # Network Load Balancer por ambiente
│   ├── apigateway-vpc-link.tf   # VPC Link para API Gateway
│   ├── configmap.tf             # ConfigMaps dinâmicos injetados no K8s
│   ├── secrets.tf               # Secrets (JWT, DB, notification)
│   ├── namespace.tf             # Criação de namespace por workspace
│   ├── datadog.tf               # Integração com Datadog (opcional)
│   ├── environments/            # Variáveis por ambiente (.tfvars)
│   │   ├── dev.tfvars
│   │   └── prod.tfvars
│   └── scripts/
│       └── update_kustomize.sh  # Atualiza tags de imagem + aplica Kustomize
│
├── k8s/                         # Manifests Kubernetes (Kustomize)
│   ├── base/                    # Recursos comuns a todos os ambientes
│   │   ├── kustomization.yaml
│   │   ├── postgres.yaml        # StatefulSet (apenas local)
│   │   ├── eureka-server.yaml   # Service Discovery
│   │   ├── auth-service.yaml    # Autenticação JWT
│   │   ├── customer-service.yaml
│   │   ├── catalog-service.yaml
│   │   ├── inventory-service.yaml
│   │   ├── budget-service.yaml
│   │   ├── work-order-service.yaml
│   │   ├── notification-service.yaml
│   │   ├── configmap-shared.yaml
│   │   └── hpa.yaml             # Horizontal Pod Autoscaler
│   │
│   └── overlays/                # Configurações específicas por ambiente
│       ├── local/               # Minikube (namespace: oficina)
│       │   ├── kustomization.yaml
│       │   ├── namespace.yaml
│       │   ├── secret-*.yaml    # Secrets hardcoded para dev local
│       │   └── ingress.yaml     # Host: oficina.local
│       │
│       ├── dev/                 # EKS Dev (namespace: oficina-mecanica-dev)
│       │   ├── kustomization.yaml
│       │   ├── namespace.yaml
│       │   └── ingress.yaml     # NLB interno, sem PostgreSQL
│       │
│       └── prod/                # EKS Prod (namespace: oficina-mecanica-prod)
│           ├── kustomization.yaml
│           ├── namespace.yaml
│           ├── ingress.yaml
│           └── patches/         # Patches para prod (réplicas, recursos)
│
├── shared-library/              # Código compartilhado (DTOs, security, mappers)
│   └── src/main/java/br/com/fiap/oficina/shared/
│       ├── dto/                 # Data Transfer Objects
│       ├── validator/           # Validações customizadas
│       ├── security/            # JWT, CORS, configs de segurança
│       ├── exception/           # Exception handlers globais
│       ├── mapper/              # MapStruct mappers
│       └── constants/           # Constantes e enums
│
├── eureka-server/               # Service Discovery + Swagger agregado
├── auth-service/                # Autenticação JWT + usuários
├── customer-service/            # Clientes e veículos
├── catalog-service/             # Catálogo de produtos/serviços
├── inventory-service/           # Controle de estoque
├── budget-service/              # Orçamentos
├── work-order-service/          # Ordens de serviço
├── notification-service/        # Notificações assíncronas (email)
│
├── docs/                        # Documentação técnica
│   ├── ROUTING-ARCHITECTURE.md  # Detalhes do roteamento (API Gateway + NLB)
│   ├── KUBERNETES-IMAGE-UPDATE-STRATEGY.md
│   ├── SEED-DATA-AND-API-TESTS.md
│   └── SWAGGER-AGGREGATION.md
│
├── api-docs/                    # OpenAPI specs (JSON) baixados dos serviços
├── docker-compose.yml           # Dev local (perfil: dev)
├── docker-compose.prod.yml      # Prod simulado (perfil: prod)
├── Makefile                     # Comandos para build/push de imagens
└── pom.xml                      # POM raiz (aggregator)
```

### Conceitos Importantes

#### Shared Library
Módulo Maven compartilhado que contém código reutilizável entre todos os microserviços:
- **DTOs**: Objetos de transferência de dados padronizados
- **Security**: JWT token utils, CORS config, authentication filters
- **Validators**: Validações customizadas (CPF, CNPJ, email)
- **Exceptions**: Handlers globais para respostas de erro consistentes
- **Mappers**: MapStruct para conversão entre entidades e DTOs
- **Constants**: Enums, constantes, mensagens de erro

Todos os microserviços dependem dessa biblioteca via Maven dependency.

#### Kustomize Base + Overlays
- **base/**: Define recursos Kubernetes comuns (Deployments, Services, ConfigMaps, HPA)
- **overlays/**: Customiza a base para cada ambiente via patches (réplicas, recursos, secrets, ingress)
- Permite reutilização de código e separação clara de ambientes sem duplicação

#### Terraform Workspaces
- **dev**: Ambiente de desenvolvimento (NLB interno, 1 réplica, secrets simples)
- **prod**: Ambiente de produção (NLB público, 2+ réplicas, secrets gerenciados)
- Cada workspace mantém estado isolado no S3 backend

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### 📋 Diretrizes de Contribuição

Antes de contribuir, leia [AGENTS.md](AGENTS.md) para entender:
- Estrutura de pacotes e módulos
- Convenções de código e nomenclatura
- Como executar testes (`mvn test`, `mvn verify -Pverify-coverage`)
- Requisitos de cobertura de código (≥50% PR, ≥70% main)
- Padrão de commits e PRs

## 🚀 Roadmap e Melhorias Futuras

### CI/CD
- [ ] GitHub Actions para build automático
- [ ] Pipeline de deploy automatizado (dev → staging → prod)
- [ ] Testes de integração automatizados
- [ ] Validação de cobertura de código no PR

### Infraestrutura
- [ ] AWS Secrets Manager para prod (substituir Kubernetes Secrets)
- [ ] RDS PostgreSQL provisionado via Terraform
- [ ] Route53 + Certificate Manager para HTTPS
- [ ] CloudWatch Logs integration
- [ ] Backup automatizado de banco de dados

### Observabilidade
- [ ] Datadog APM completo
- [ ] Distributed tracing (Sleuth + Zipkin ou OpenTelemetry)
- [ ] Dashboards Grafana + Prometheus
- [ ] Alertas proativos (SLA violations, latência, erros)

### Segurança
- [ ] OAuth2 / OpenID Connect integration
- [ ] Rate limiting no API Gateway
- [ ] WAF (Web Application Firewall)
- [ ] Secrets rotation automatizado
- [ ] Vulnerability scanning (Snyk, Trivy)

### Features de Aplicação
- [ ] Webhooks para notificações
- [ ] Integração com sistemas de pagamento
- [ ] Relatórios e analytics
- [ ] Mobile app integration
- [ ] Sistema de chat/suporte

---

## 📞 Suporte

Para dúvidas ou problemas:
1. Verifique a seção [Troubleshooting](#-troubleshooting)
2. Consulte a [documentação técnica](docs/)
3. Abra uma issue no GitHub
4. Entre em contato com a equipe de desenvolvimento

---

**Desenvolvido com ❤️ pela equipe FIAP - Oficina Microservices**
