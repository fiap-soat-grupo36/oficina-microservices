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

## 🚀 Como Executar

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
# Construir e iniciar todos os serviços
docker-compose up -d

# Verificar o status dos serviços
docker-compose ps

# Visualizar logs
docker-compose logs -f

# Parar todos os serviços
docker-compose down
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
# Reconstruir imagens
docker-compose build --no-cache

# Limpar volumes
docker-compose down -v

# Reiniciar tudo
docker-compose up -d --force-recreate
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
├── docker-compose.yml      # Configuração Docker
└── pom.xml                 # POM raiz
```

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request
