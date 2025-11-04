# 🐳 Docker Setup

## Pré-requisitos
- Docker 20.10+
- Docker Compose 2.0+
- 8GB RAM disponível (mínimo)

## Comandos

### Iniciar todos os serviços
```bash
docker-compose up -d
```

### Parar todos os serviços
```bash
docker-compose down
```

### Ver logs de um serviço específico
```bash
docker-compose logs -f eureka-server
docker-compose logs -f work-order-service
```

### Rebuild após mudanças no código
```bash
docker-compose up -d --build
```

### Limpar tudo (containers, volumes, imagens)
```bash
docker-compose down -v --rmi all
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
- Auth Service: http://localhost:8082/swagger-ui.html
- Customer Service: http://localhost:8081/swagger-ui.html
- Catalog Service: http://localhost:8083/swagger-ui.html
- Inventory Service: http://localhost:8084/swagger-ui.html
- Budget Service: http://localhost:8085/swagger-ui.html
- Work Order Service: http://localhost:8086/swagger-ui.html

## Troubleshooting

### Serviço não registra no Eureka
```bash
# Verifique se o Eureka está UP
curl http://localhost:8761/actuator/health

# Verifique os logs do serviço
docker-compose logs -f {service-name}
```

### Erro de memória
Aumente a memória disponível para o Docker nas configurações.

### Rebuild apenas um serviço
```bash
docker-compose up -d --build customer-service
```
