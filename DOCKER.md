# 🐳 Docker Setup

## Pré-requisitos
- Docker 20.10+
- Docker Compose 2.0+
- 8GB RAM disponível (mínimo)

## 💾 Banco de Dados

O Docker Compose sobe um Postgres compartilhado (`postgres`) para todos os serviços.

**Config padrão**
- Banco: `oficina-db`
- Usuário/Senha: `postgres` / `postgres`
- Porta exposta: `5432`
- Volume: `postgres_data` (mantém os dados entre recriações de container)

**Perfis e migrações**
- Perfil `dev` recria e popula o banco (`DDL_AUTO=create-drop`, `SQL_INIT_MODE=always`)
- Perfil `prod` preserva dados e não roda seeds (`DDL_AUTO=update`, `SQL_INIT_MODE=never`)
- Personalize passando variáveis: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DDL_AUTO`, `SQL_INIT_MODE`, `SHOW_SQL`

## Comandos

### Subir ambiente de desenvolvimento (perfil dev - build local)
```bash
docker compose --profile dev up -d
```

### Subir ambiente de produção (perfil prod - imagens publicadas)
```bash
REGISTRY=seu-usuario TAG=latest docker compose --profile prod -f docker-compose.yml -f docker-compose.prod.yml up -d
```
> `REGISTRY` usa `grecomilani` por padrão e `TAG` assume `latest`.

### Parar todos os serviços
```bash
docker compose --profile dev down
docker compose --profile prod -f docker-compose.yml -f docker-compose.prod.yml down
```

### Ver logs de um serviço específico
```bash
docker compose --profile dev logs -f eureka-server
docker compose --profile prod -f docker-compose.yml -f docker-compose.prod.yml logs -f work-order-service
```

### Rebuild após mudanças no código (dev)
```bash
docker compose --profile dev up -d --build
```

### Limpar tudo (containers, volumes, imagens) no perfil dev
```bash
docker compose --profile dev down -v --rmi all
```

## Ordem de Inicialização

1. **eureka-server** (8761) - Service Discovery
2. **auth-service** (8082) - Autenticação
3. **customer-service** (8081) - Clientes e Veículos
4. **catalog-service** (8083) - Serviços e Produtos
5. **inventory-service** (8084) - Estoque
6. **budget-service** (8085) - Orçamentos
7. **work-order-service** (8086) - Ordens de Serviço
8. **notification-service** (8087) - Notificações

## Acessos

- Eureka Dashboard: http://localhost:8761
- Swagger Agregado: http://localhost:8761/swagger-ui.html
- Auth Service: http://localhost:8082/swagger-ui.html
- Customer Service: http://localhost:8081/swagger-ui.html
- Catalog Service: http://localhost:8083/swagger-ui.html
- Inventory Service: http://localhost:8084/swagger-ui.html
- Budget Service: http://localhost:8085/swagger-ui.html
- Work Order Service: http://localhost:8086/swagger-ui.html

### Swagger Agregado

O Swagger agregado no Eureka Server permite visualizar todas as APIs em um único local:

- **Acesso pelo navegador**: As URLs usam `localhost` para que o navegador possa acessá-las
- **Comunicação interna**: Os microserviços continuam se comunicando via nomes dos serviços Docker
- **CORS habilitado**: Configuração global na `shared-library` permite requisições cross-origin

## Troubleshooting

### Serviço não registra no Eureka
```bash
# Verifique se o Eureka está UP
curl http://localhost:8761/actuator/health

# Verifique os logs do serviço
docker compose --profile dev logs -f {service-name}
```

### Erro de memória
Aumente a memória disponível para o Docker nas configurações.

### Rebuild apenas um serviço
```bash
docker compose --profile dev up -d --build customer-service
```
