# Oficina Microservices

Arquitetura de microserviços para sistema de gestão de oficina mecânica.

## 📋 Arquitetura

Este projeto utiliza uma arquitetura de microserviços com as seguintes características:

### Service Discovery - Netflix Eureka

Todos os microserviços se registram automaticamente no **Eureka Server**, permitindo comunicação dinâmica entre serviços sem URLs hardcoded.

**Dashboard do Eureka:** http://localhost:8761

### Microserviços

- **eureka-server** (porta 8761) - Service Registry
- **auth-service** (porta 8082) - Autenticação e gerenciamento de usuários
- **customer-service** (porta 8081) - Gestão de clientes e veículos
- **catalog-service** (porta 8083) - Catálogo de serviços e produtos
- **inventory-service** (porta 8084) - Controle de estoque
- **budget-service** (porta 8085) - Gestão de orçamentos
- **work-order-service** (porta 8086) - Ordens de serviço
- **notification-service** (porta 8087) - Notificações por email

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

- **Spring Boot 3.5.3** - Framework principal
- **Spring Cloud 2025.0.0** - Cloud native patterns
- **Netflix Eureka** - Service Discovery
- **OpenFeign** - Comunicação entre microserviços
- **PostgreSQL** - Banco de dados
- **H2** - Banco de dados em memória (testes)
- **Java 21** - Linguagem de programação
- **Maven** - Gerenciamento de dependências
- **Docker** - Containerização

## 📚 Documentação da API

### Swagger Agregado

Para facilitar o acesso à documentação de todos os microserviços em um único local, o **Eureka Server** disponibiliza um Swagger agregado:

**🔗 Acesso único:** http://localhost:8761/swagger-ui.html

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

## 📝 Estrutura do Projeto

```
oficina-microservices/
├── eureka-server/          # Service Discovery
├── shared-library/         # Código compartilhado
├── auth-service/           # Autenticação
├── customer-service/       # Clientes e veículos
├── catalog-service/        # Catálogo
├── inventory-service/      # Estoque
├── budget-service/         # Orçamentos
├── work-order-service/     # Ordens de serviço
├── notification-service/   # Notificações
├── docker-compose.yml      # Configuração Docker (perfil dev)
├── docker-compose.prod.yml # Overrides para perfil prod
└── pom.xml                 # POM raiz
```

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request
