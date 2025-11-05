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
