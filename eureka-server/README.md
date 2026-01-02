# Eureka Server

Netflix Eureka Server para Service Discovery dos microserviços da Oficina.

## 📚 Documentação Completa

- **[QUICKSTART-NLB.md](QUICKSTART-NLB.md)** - Guia rápido para deploy com NLB
- **[README-NLB.md](README-NLB.md)** - Documentação completa da integração com NLB
- **[../ENVIRONMENT-SETUP-GUIDE.md](../ENVIRONMENT-SETUP-GUIDE.md)** - Configuração por ambiente (local/dev/prod)
- **[../USING-EXISTING-NLB.md](../USING-EXISTING-NLB.md)** - Como usar um NLB existente
- **[../EUREKA-CLIENT-SETUP.md](../EUREKA-CLIENT-SETUP.md)** - Como conectar microservices ao Eureka

## Descrição

Este módulo implementa o Eureka Server que atua como um registro de serviços (Service Registry) permitindo que todos os microserviços da arquitetura se registrem dinamicamente e se comuniquem entre si sem a necessidade de URLs hardcoded.

### 🌐 Deployment por Ambiente

| Ambiente | Service Discovery | Alta Disponibilidade | Acesso Externo |
|----------|------------------|---------------------|----------------|
| **Local (Minikube)** | ClusterIP interno | ❌ | Port-forward |
| **Dev (EKS)** | NLB Internal | ✅ | Apenas VPC |
| **Prod (EKS)** | NLB Internet-facing | ✅✅ (2 replicas) | Público + Route53 |

## Tecnologias

- Spring Boot 3.5.3
- Spring Cloud 2025.0.0
- Netflix Eureka Server
- Java 21

## Configuração

O servidor está configurado para:
- Executar na porta **8761**
- Não se registrar como cliente Eureka (`register-with-eureka: false`)
- Não buscar registro de outros servidores Eureka (`fetch-registry: false`)
- Auto-preservação desabilitada para ambiente de desenvolvimento

## Como Executar

### Localmente

```bash
# Na raiz do projeto
mvn clean install -DskipTests
cd eureka-server
mvn spring-boot:run
```

### Com Docker

```bash
# Na raiz do projeto
docker-compose up eureka-server
```

## Acessar Dashboard

Após iniciar o serviço, acesse o dashboard do Eureka:

**URL:** http://localhost:8761

No dashboard você pode visualizar:
- Todos os serviços registrados
- Status de cada instância
- Informações sobre réplicas (em ambientes de produção)
- Metadata de cada serviço

## Endpoints

- **Dashboard:** http://localhost:8761
- **Health Check:** http://localhost:8761/actuator/health
- **Eureka API:** http://localhost:8761/eureka/apps

## Ordem de Inicialização

O Eureka Server deve ser iniciado **antes** dos demais microserviços para que eles possam se registrar corretamente:

1. Eureka Server (este módulo)
2. Demais microserviços (auth-service, customer-service, etc.)

## Troubleshooting

### Serviços não aparecem no dashboard

1. Verifique se o Eureka Server está executando
2. Confirme que os serviços têm a dependência `spring-cloud-starter-netflix-eureka-client`
3. Verifique se a configuração `eureka.client.service-url.defaultZone` está correta nos serviços
4. Aguarde até 30 segundos - o registro pode levar alguns segundos

### Erro de conexão

Certifique-se de que:
- A porta 8761 está disponível
- Não há firewall bloqueando a porta
- A URL configurada nos clientes corresponde ao endereço do servidor

## Referências

- [Spring Cloud Netflix Eureka](https://cloud.spring.io/spring-cloud-netflix/reference/html/)
- [Eureka Wiki](https://github.com/Netflix/eureka/wiki)
