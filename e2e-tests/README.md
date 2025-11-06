# 🧪 Testes E2E - Oficina Microservices

Scripts automatizados para realizar testes End-to-End completos do sistema de oficina mecânica.

## 📋 Pré-requisitos

- **Docker e Docker Compose** instalados
- **jq** - Processador JSON para linha de comando
- **curl** - Cliente HTTP
- Todos os microserviços rodando

### Instalar Dependências

#### Ubuntu/Debian
```bash
sudo apt-get update
sudo apt-get install jq curl -y
```

#### macOS
```bash
brew install jq
```

#### Windows (WSL)
```bash
sudo apt-get install jq curl -y
```

## 🚀 Como Usar

### 1. Iniciar os Serviços

```bash
# Na raiz do projeto
docker-compose up -d

# Aguardar todos os serviços ficarem prontos (1-2 minutos)
docker-compose ps
```

### 2. Executar Testes E2E

```bash
cd e2e-tests

# Dar permissões de execução
chmod +x *.sh

# Executar testes completos
./run-tests.sh
```

### 3. Executar Apenas o Script de Teste

```bash
./test-e2e.sh
```

### 4. Limpar Dados de Teste

```bash
./cleanup.sh
```

## 📊 Fluxo de Teste

O script executa o seguinte fluxo completo:

1. ✅ **Verificação de Serviços** - Confirma que todos os microserviços estão online
2. 👤 **Criar Admin** - Cria usuário administrador
3. 🔐 **Autenticação** - Obtém token JWT
4. 👨‍🔧 **Criar Mecânico** - Cadastra mecânico no sistema
5. 👥 **Criar Cliente** - Cadastra cliente com endereço completo
6. 🚗 **Criar Veículo** - Cadastra veículo do cliente
7. 🔧 **Criar Serviços** - Adiciona serviços ao catálogo
8. 📦 **Criar Produtos** - Adiciona produtos ao catálogo
9. 📊 **Adicionar Estoque** - Registra entrada de produtos no estoque
10. 📋 **Criar Ordem de Serviço** - Abre OS para o veículo
11. 👨‍🔧 **Atribuir Mecânico** - Atribui mecânico à OS
12. 🔧 **Adicionar Serviços à OS** - Vincula serviços necessários
13. 📦 **Adicionar Produtos à OS** - Vincula produtos necessários
14. 🔒 **Reservar Estoque** - Reserva produtos no estoque
15. 🔍 **Diagnosticar OS** - Gera orçamento automaticamente
16. 💰 **Buscar Orçamento** - Consulta orçamento gerado
17. ✅ **Aprovar Orçamento** - Cliente aprova o orçamento
18. ⚙️ **Executar Serviço** - Inicia execução dos serviços
19. 🏁 **Finalizar Serviço** - Marca serviço como concluído
20. 🚗 **Entregar Veículo** - Registra entrega ao cliente
21. 📊 **Verificar Estoque** - Confirma atualização do estoque

## 📁 Arquivos Gerados

Após a execução, os seguintes arquivos são criados em `results/`:

- `admin.json` - Dados do admin criado
- `token.json` - Token JWT
- `mecanico.json` - Dados do mecânico
- `cliente.json` - Dados do cliente
- `veiculo.json` - Dados do veículo
- `servico1.json`, `servico2.json` - Serviços criados
- `produto1.json`, `produto2.json` - Produtos criados
- `ordem-servico.json` - OS criada
- `orcamento.json` - Orçamento gerado
- `ids.env` - Todos os IDs para reutilização

## ⚙️ Configuração

Edite o arquivo `config.env` para ajustar:

- URLs dos microserviços
- Credenciais de admin/mecânico
- Configurações de email

```bash
# Exemplo de alteração de porta
AUTH_SERVICE="http://localhost:8082"
CUSTOMER_SERVICE="http://localhost:8081"
```

## 🔍 Verificação de Emails

Durante o teste, 2 emails devem ser enviados:

1. 📧 **Orçamento Disponível** - Após diagnosticar a OS
2. 📧 **Veículo Pronto** - Após finalizar a OS

Verifique a caixa de entrada do email configurado no `notification-service`.

## 🐛 Troubleshooting

### Serviços Offline

```bash
# Verificar status dos serviços
docker-compose ps

# Ver logs de um serviço específico
docker-compose logs auth-service
```

### Erro de Permissão

```bash
chmod +x *.sh
```

### JQ não instalado

```bash
# Ubuntu/Debian
sudo apt-get install jq

# macOS
brew install jq
```

### Token Expirado

O token JWT expira após 1 hora. Execute o script novamente:

```bash
./test-e2e.sh
```

### Limpar e Recomeçar

```bash
# Limpar dados de teste
./cleanup.sh

# Resetar bancos de dados
docker-compose down -v
docker-compose up -d
```

## 📊 Exemplo de Saída

```
╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║     🔧 TESTE E2E - OFICINA MICROSERVICES 🔧                   ║
║                                                               ║
║  Teste automatizado completo do fluxo de ordem de serviço    ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝

========================================
VERIFICANDO SERVIÇOS
========================================

ℹ️  Verificando Auth Service...
✅ Auth Service está online
...

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ TESTE E2E CONCLUÍDO COM SUCESSO!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📊 Resumo dos IDs Criados:
  • Cliente ID:      1
  • Veículo ID:      1
  • Mecânico ID:     2
  • Ordem Serviço:   1
```

## 🔗 Links Úteis

- **Swagger Agregado**: http://localhost:8761/swagger-ui.html
- **Eureka Dashboard**: http://localhost:8761
- **Auth Service**: http://localhost:8082/swagger-ui.html
- **Customer Service**: http://localhost:8081/swagger-ui.html
- **Catalog Service**: http://localhost:8083/swagger-ui.html
- **Inventory Service**: http://localhost:8084/swagger-ui.html
- **Budget Service**: http://localhost:8085/swagger-ui.html
- **Work Order Service**: http://localhost:8086/swagger-ui.html

## 📝 Categorias Válidas

**Serviços (CategoriaServico):**
- `MECANICO` - Serviços mecânicos gerais
- `ELETRICO` - Serviços elétricos
- `FREIOS` - Serviços de freios
- `ALINHAMENTO` - Alinhamento e balanceamento
- `SUSPENSAO` - Serviços de suspensão

**Produtos (CategoriaProduto):**
- `PECA` - Peças e componentes
- `INSUMO` - Insumos e materiais consumíveis

## 📝 Notas

- Os testes criam dados reais no banco
- Use o script `cleanup.sh` para limpar após os testes
- Os IDs são salvos em `results/ids.env` para reutilização
- Logs detalhados são salvos em `results/*.json`

## 🤝 Contribuindo

Para adicionar novos cenários de teste:

1. Edite `test-e2e.sh`
2. Adicione novas funções de teste
3. Chame a função no `main()`
4. Teste localmente antes de commitar

## 📄 Licença

Este projeto está sob a licença MIT.
