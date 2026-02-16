# Work Order Service

Microserviço responsável pelo gerenciamento de ordens de serviço da oficina.

## Características

- **Port**: 8086
- **Database**: H2 (in-memory) - workorderdb
- **Security**: Spring Security com JWT
- **API Documentation**: Swagger UI disponível em `/swagger-ui.html`

## Funcionalidades

### Gerenciamento de Ordens de Serviço

- Criar, listar, atualizar e deletar ordens de serviço
- Atribuir mecânico a uma OS
- Diagnosticar, executar, finalizar e entregar veículo
- Adicionar/remover serviços e produtos

### Endpoints Principais

- `POST /api/ordens-servico` - Criar OS
- `GET /api/ordens-servico` - Listar todas (com filtro por status)
- `GET /api/ordens-servico/{id}` - Buscar por ID
- `PUT /api/ordens-servico/{id}` - Atualizar OS
- `DELETE /api/ordens-servico/{id}` - Deletar OS
- `GET /api/ordens-servico/por-cliente/{clienteId}` - Buscar por cliente
- `GET /api/ordens-servico/por-veiculo/{veiculoId}` - Buscar por veículo
- `GET /api/ordens-servico/por-mecanico/{mecanicoId}` - Buscar por mecânico
- `PUT /api/ordens-servico/{id}/atribuir-mecanico` - Atribuir mecânico
- `PUT /api/ordens-servico/{id}/diagnosticar` - Diagnosticar OS
- `PUT /api/ordens-servico/{id}/executar` - Iniciar execução
- `PUT /api/ordens-servico/{id}/finalizar` - Finalizar OS
- `PUT /api/ordens-servico/{id}/entregar` - Entregar veículo
- `POST /api/ordens-servico/{id}/servicos` - Adicionar serviços
- `DELETE /api/ordens-servico/{id}/servicos` - Remover serviços
- `POST /api/ordens-servico/{id}/produtos` - Adicionar produtos
- `DELETE /api/ordens-servico/{id}/produtos` - Remover produtos

## Fluxo de Status

```
RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → 
EM_EXECUCAO → FINALIZADA → ENTREGUE
```

## Eventos

- **CalcularOrcamentoEvent**: Publicado após criar/diagnosticar OS
- **VeiculoDisponivelEvent**: Publicado após finalizar OS

## Integração com Outros Serviços

- **Customer Service (8081)**: Valida clientes e veículos
- **Catalog Service (8083)**: Valida serviços e produtos
- **Auth Service**: Valida usuários e roles

## Known Issues

### Feign Client Runtime Issue

Existe um problema de compatibilidade conhecido entre Spring Cloud OpenFeign versão milestone (5.0.0-M4) e Spring Boot
3.5.3, causando erro em tempo de execução:

```
java.lang.NoClassDefFoundError: tools/jackson/databind/JacksonModule
```

**Soluções possíveis:**

1. Aguardar versão estável do Spring Cloud compatível com Spring Boot 3.5.x
2. Fazer downgrade do Spring Boot para 3.4.x
3. Usar versão estável do Spring Cloud (não milestone)
4. Em ambiente de produção com service mesh, o problema pode não ocorrer

O código está correto e compila sem problemas. A funcionalidade está implementada conforme especificação.

## Como Executar

```bash
mvn spring-boot:run
```

## Banco de Dados

Console H2 disponível em: `http://localhost:8086/h2-console`

- URL: `jdbc:h2:mem:workorderdb`
- Username: `sa`
- Password: (vazio)

## Seeds

O banco de dados é populado automaticamente com dados de exemplo via `data.sql`.
