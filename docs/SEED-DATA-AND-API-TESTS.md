# Seed Data e Testes de API - Oficina Microservices

## Dados de Seed Configurados

### Auth Service (import.sql)

O `auth-service` possui 5 usuários pré-cadastrados:

| Username | Senha | Role | Nome |
|----------|-------|------|------|
| admin | admin123 | ADMIN | Administrador do Sistema |
| cliente | cliente123 | CLIENTE | Cliente Teste |
| mecanico | mecanico123 | MECANICO | Mecânico Teste |
| atendente | atendente123 | ATENDENTE | Atendente Teste |
| estoquista | estoquista123 | ESTOQUISTA | Estoquista Teste |

**Arquivo:** `auth-service/src/main/resources/import.sql`

```sql
INSERT INTO usuarios (username, nome, password, role, ativo)
VALUES ('admin', 'Administrador do Sistema', '$2a$10$...', 'ADMIN', true)
ON CONFLICT (username) DO NOTHING;
-- ... outros usuários
```

### Outros Services

Os demais microservices **NÃO possuem dados de seed configurados**:
- ❌ customer-service
- ❌ catalog-service
- ❌ inventory-service
- ❌ budget-service
- ❌ work-order-service
- ❌ notification-service

## Testes de API Realizados ✅

### 1. Auth Service

**Endpoint Base:** `https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/auth`

#### ✅ Login (POST /api/auth/login)

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/auth/api/auth/login
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### ✅ Listar Usuários (GET /api/usuarios)

```bash
TOKEN="<jwt-token>"
curl -H "Authorization: Bearer $TOKEN" \
  https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/auth/api/usuarios
```

**Resposta:**
```json
[
  {
    "id": 1,
    "username": "admin",
    "nome": "Administrador do Sistema",
    "role": "ADMIN"
  },
  // ... outros usuários
]
```

#### ✅ Todos os Logins Testados

| Usuário | Status |
|---------|--------|
| admin | ✅ Funciona |
| cliente | ✅ Funciona |
| mecanico | ✅ Funciona |
| atendente | ✅ Funciona |
| estoquista | ✅ Funciona |

### 2. Customer Service

**Endpoint Base:** `https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/customer`

#### ✅ Criar Cliente (POST /api/clientes)

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "cpf": "12345678901",
    "email": "joao.silva@example.com",
    "telefone": "11987654321",
    "endereco": {
      "logradouro": "Rua das Flores",
      "numero": "123",
      "complemento": "Apt 45",
      "bairro": "Centro",
      "cidade": "São Paulo",
      "estado": "SP",
      "cep": "01234567"
    }
  }' \
  https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/customer/api/clientes
```

**Resposta:**
```json
{
  "id": 1,
  "nome": "João Silva",
  "cpf": "123.456.789-01",
  "email": "joao.silva@example.com",
  "telefone": "11987654321",
  "endereco": {
    "logradouro": "Rua das Flores",
    "numero": "123",
    // ...
  }
}
```

#### ✅ Criar Veículo (POST /api/veiculos)

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "placa": "ABC1234",
    "marca": "Toyota",
    "modelo": "Corolla",
    "ano": 2022,
    "cor": "Prata"
  }' \
  https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/customer/api/veiculos
```

**Resposta:**
```json
{
  "id": 1,
  "placa": "ABC1234",
  "marca": "Toyota",
  "modelo": "Corolla",
  "ano": 2022,
  "cor": "Prata",
  "clienteId": 1,
  "ativo": true
}
```

#### ✅ Listar Veículos do Cliente (GET /api/veiculos/cliente/{id})

```bash
curl -H "Authorization: Bearer $TOKEN" \
  https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/customer/api/veiculos/cliente/1
```

### 3. Catalog Service

**Endpoint Base:** `https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/catalog`

#### ✅ Criar Serviço (POST /api/servicos)

**Categorias válidas:** `ALINHAMENTO`, `SUSPENSAO`, `FREIOS`, `MECANICO`, `ELETRICO`

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Troca de Óleo",
    "descricao": "Troca completa de óleo do motor com filtro",
    "categoria": "MECANICO",
    "precoBase": 150.00,
    "tempoEstimadoMinutos": 60
  }' \
  https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/catalog/api/servicos
```

**Resposta:**
```json
{
  "id": 1,
  "nome": "Troca de Óleo",
  "descricao": "Troca completa de óleo do motor com filtro",
  "categoria": "MECANICO",
  "precoBase": 150.00,
  "tempoEstimadoMinutos": 60,
  "ativo": true
}
```

#### ✅ Listar Serviços (GET /api/servicos)

```bash
curl -H "Authorization: Bearer $TOKEN" \
  https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/catalog/api/servicos
```

#### ✅ Serviços Criados no Teste

| ID | Nome | Categoria | Preço | Tempo |
|----|------|-----------|-------|-------|
| 1 | Troca de Óleo | MECANICO | R$ 150,00 | 60min |
| 2 | Alinhamento e Balanceamento | ALINHAMENTO | R$ 120,00 | 90min |
| 3 | Revisão de Freios | FREIOS | R$ 200,00 | 120min |

### 4. Inventory Service

**Endpoint Base:** `https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/inventory`

#### ⚠️ Status

- ✅ Endpoint responde (HTTP 204)
- ❌ Endpoint de produtos não encontrado
- ⏸️ Testes de criação pendentes

### 5. Budget Service

**Endpoint Base:** `https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/budget`

#### ✅ Listar Orçamentos (GET /api/orcamentos)

```bash
curl -H "Authorization: Bearer $TOKEN" \
  https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/budget/api/orcamentos
```

**Resposta:** `[]` (vazio, mas funcionando)

### 6. Work Order Service

**Endpoint Base:** `https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/work-order`

#### ✅ Listar Ordens de Serviço (GET /api/ordens-servico)

```bash
curl -H "Authorization: Bearer $TOKEN" \
  https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/work-order/api/ordens-servico
```

**Resposta:** `[]` (vazio, mas funcionando)

### 7. Notification Service

**Endpoint Base:** `https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/notification`

#### ✅ Health Check (GET /actuator/health)

```bash
curl https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/notification/actuator/health
```

**Resposta:**
```json
{
  "status": "UP",
  "groups": ["liveness", "readiness"]
}
```

## Resumo dos Testes

| Microservice | Status | Endpoints Testados | Seed Data |
|--------------|--------|-------------------|-----------|
| ✅ auth-service | Funcional | Login, Listar Usuários | ✅ 5 usuários |
| ✅ customer-service | Funcional | Criar/Listar Clientes, Criar/Listar Veículos | ❌ Nenhum |
| ✅ catalog-service | Funcional | Criar/Listar Serviços | ❌ Nenhum |
| ⚠️ inventory-service | Parcial | GET responde 204 | ❌ Nenhum |
| ✅ budget-service | Funcional | Listar (vazio) | ❌ Nenhum |
| ✅ work-order-service | Funcional | Listar (vazio) | ❌ Nenhum |
| ✅ notification-service | Funcional | Health Check | N/A |

## Como Executar os Testes

### 1. Obter Token JWT

```bash
TOKEN=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/auth/api/auth/login \
  | jq -r '.token')

echo $TOKEN
```

### 2. Testar Endpoints

Use o token em todas as requisições:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com/<service>/<endpoint>
```

## Validação Pós-Deploy

Após um novo deploy, execute este script para validar todos os services:

```bash
#!/bin/bash
set -e

API_BASE="https://d6l9d5prg2.execute-api.us-east-2.amazonaws.com"

echo "🔐 Obtendo token..."
TOKEN=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  $API_BASE/auth/api/auth/login | jq -r '.token')

echo "✅ Token obtido!"

echo ""
echo "📋 Testando serviços..."

# Auth
echo -n "auth-service: "
curl -s -H "Authorization: Bearer $TOKEN" $API_BASE/auth/api/usuarios > /dev/null && echo "✅" || echo "❌"

# Customer
echo -n "customer-service: "
curl -s -H "Authorization: Bearer $TOKEN" $API_BASE/customer/api/clientes -w "%{http_code}" -o /dev/null | grep -q "20" && echo "✅" || echo "❌"

# Catalog
echo -n "catalog-service: "
curl -s -H "Authorization: Bearer $TOKEN" $API_BASE/catalog/api/servicos -w "%{http_code}" -o /dev/null | grep -q "20" && echo "✅" || echo "❌"

# Inventory
echo -n "inventory-service: "
curl -s -H "Authorization: Bearer $TOKEN" $API_BASE/inventory/api/estoque -w "%{http_code}" -o /dev/null | grep -q "20" && echo "✅" || echo "❌"

# Budget
echo -n "budget-service: "
curl -s -H "Authorization: Bearer $TOKEN" $API_BASE/budget/api/orcamentos -w "%{http_code}" -o /dev/null | grep -q "200" && echo "✅" || echo "❌"

# Work Order
echo -n "work-order-service: "
curl -s -H "Authorization: Bearer $TOKEN" $API_BASE/work-order/api/ordens-servico -w "%{http_code}" -o /dev/null | grep -q "200" && echo "✅" || echo "❌"

# Notification
echo -n "notification-service: "
curl -s $API_BASE/notification/actuator/health | grep -q "UP" && echo "✅" || echo "❌"

echo ""
echo "✨ Validação concluída!"
```

## Problemas Conhecidos

### 1. Endpoint de Produtos

**Erro:** `No static resource api/produtos`

**Status:** Endpoint pode não estar implementado ou caminho incorreto

### 2. Categorias de Serviço

**Valores aceitos:** `ALINHAMENTO`, `SUSPENSAO`, `FREIOS`, `MECANICO`, `ELETRICO`

**Erro comum:** Usar `MANUTENCAO_PREVENTIVA` ou outras categorias não mapeadas

### 3. Campos Obrigatórios

Ao criar serviços, é necessário:
- ✅ `precoBase` (não `preco`)
- ✅ `categoria` (enum válida)
- ✅ `nome`
- ✅ `descricao`

## Dados de Teste Criados

Durante os testes, foram criados os seguintes dados:

### Cliente
- ID: 1
- Nome: João Silva
- CPF: 123.456.789-01
- Email: joao.silva@example.com

### Veículo
- ID: 1
- Placa: ABC1234
- Marca: Toyota
- Modelo: Corolla (2022)
- Cliente: João Silva

### Serviços
1. Troca de Óleo - R$ 150,00 (60min)
2. Alinhamento e Balanceamento - R$ 120,00 (90min)
3. Revisão de Freios - R$ 200,00 (120min)

## Recomendações

1. **Adicionar seed data** nos outros microservices para facilitar testes
2. **Criar script de validação** automatizado no CI/CD
3. **Documentar todos os endpoints** no Swagger/OpenAPI
4. **Adicionar testes de integração** end-to-end no pipeline
