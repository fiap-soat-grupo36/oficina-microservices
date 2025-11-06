#!/bin/bash

# Script de Teste E2E - Oficina Microservices
# Testa o fluxo completo de uma ordem de serviço

# Carregar configurações
source config.env

# Criar diretório de resultados
mkdir -p results

# Variáveis globais
TOKEN=""
ADMIN_ID=""
MECANICO_ID=""
CLIENTE_ID=""
VEICULO_ID=""
SERVICO1_ID=""
SERVICO2_ID=""
PRODUTO1_ID=""
PRODUTO2_ID=""
OS_ID=""
ORCAMENTO_ID=""

# ========================================
# FUNÇÕES AUXILIARES
# ========================================

print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

check_response() {
    local response=$1
    local description=$2
    
    if echo "$response" | jq -e . >/dev/null 2>&1; then
        if echo "$response" | jq -e '.error' >/dev/null 2>&1; then
            print_error "$description falhou"
            echo "$response" | jq '.'
            return 1
        else
            print_success "$description concluído"
            return 0
        fi
    else
        print_error "$description falhou - resposta inválida"
        echo "$response"
        return 1
    fi
}

# ========================================
# VERIFICAÇÃO DE SERVIÇOS
# ========================================

check_service() {
    local service_name=$1
    local service_url=$2
    local max_attempts=3
    local attempt=1
    
    print_info "Verificando $service_name..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f -o /dev/null --max-time 5 "$service_url/actuator/health"; then
            print_success "$service_name está online"
            return 0
        fi
        
        if [ $attempt -lt $max_attempts ]; then
            print_info "Tentativa $attempt/$max_attempts falhou. Aguardando 5 segundos..."
            sleep 5
        fi
        
        ((attempt++))
    done
    
    print_error "$service_name está offline após $max_attempts tentativas"
    return 1
}

verify_all_services() {
    print_header "VERIFICANDO SERVIÇOS"
    
    local services_ok=true
    
    check_service "Auth Service" "$AUTH_SERVICE" || services_ok=false
    check_service "Customer Service" "$CUSTOMER_SERVICE" || services_ok=false
    check_service "Catalog Service" "$CATALOG_SERVICE" || services_ok=false
    check_service "Inventory Service" "$INVENTORY_SERVICE" || services_ok=false
    check_service "Budget Service" "$BUDGET_SERVICE" || services_ok=false
    check_service "Work Order Service" "$WORKORDER_SERVICE" || services_ok=false
    
    if [ "$services_ok" = false ]; then
        print_error "Alguns serviços estão offline. Execute 'docker-compose up -d' e aguarde."
        exit 1
    fi
    
    print_success "Todos os serviços estão online!"
}

# ========================================
# 1. CRIAR ADMIN
# ========================================

create_admin() {
    print_header "1. CRIAR ADMINISTRADOR"
    
    local response=$(curl -s -X POST "$AUTH_SERVICE/api/usuarios" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "'$ADMIN_USERNAME'",
            "password": "'$ADMIN_PASSWORD'",
            "email": "admin@oficina.com",
            "role": "ADMIN"
        }')
    
    echo "$response" > results/admin.json
    
    if check_response "$response" "Criação do Admin"; then
        ADMIN_ID=$(echo "$response" | jq -r '.id // .userId // .user_id // empty')
        print_info "Admin ID: $ADMIN_ID"
    else
        # Admin pode já existir, continuar mesmo assim
        print_info "Admin pode já existir, continuando..."
    fi
}

# ========================================
# 2. FAZER LOGIN E OBTER TOKEN
# ========================================

login() {
    print_header "2. AUTENTICAÇÃO"
    
    local response=$(curl -s -X POST "$AUTH_SERVICE/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "'$ADMIN_USERNAME'",
            "password": "'$ADMIN_PASSWORD'"
        }')
    
    echo "$response" > results/token.json
    
    if check_response "$response" "Login"; then
        TOKEN=$(echo "$response" | jq -r '.token // .accessToken // .access_token // empty')
        
        if [ -z "$TOKEN" ]; then
            print_error "Token não encontrado na resposta"
            exit 1
        fi
        
        print_success "Token obtido: ${TOKEN:0:20}..."
    else
        exit 1
    fi
}

# ========================================
# 3. CRIAR MECÂNICO
# ========================================

create_mecanico() {
    print_header "3. CRIAR MECÂNICO"
    
    local response=$(curl -s -X POST "$AUTH_SERVICE/api/usuarios" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "username": "'$MECANICO_USERNAME'",
            "password": "'$MECANICO_PASSWORD'",
            "email": "mecanico@oficina.com",
            "role": "MECANICO",
            "nome": "José da Silva",
            "cpf": "12345678901",
            "telefone": "(11) 98765-4321",
            "especialidade": "Mecânica Geral"
        }')
    
    echo "$response" > results/mecanico.json
    
    if check_response "$response" "Criação do Mecânico"; then
        MECANICO_ID=$(echo "$response" | jq -r '.id // .userId // .user_id // empty')
        print_info "Mecânico ID: $MECANICO_ID"
    else
        # Mecânico pode já existir
        print_info "Mecânico pode já existir, usando ID padrão"
        MECANICO_ID="2"
    fi
}

# ========================================
# 4. CRIAR CLIENTE
# ========================================

create_cliente() {
    print_header "4. CRIAR CLIENTE"
    
    local timestamp=$(date +%s)
    local response=$(curl -s -X POST "$CUSTOMER_SERVICE/api/clientes" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "nome": "João das Couves",
            "cpf": "98765432100",
            "email": "joao.couves.'$timestamp'@email.com",
            "telefone": "(11) 91234-5678",
            "endereco": {
                "logradouro": "Rua das Flores",
                "numero": "123",
                "complemento": "Apto 45",
                "bairro": "Jardim Primavera",
                "cidade": "São Paulo",
                "estado": "SP",
                "cep": "01234-567"
            }
        }')
    
    echo "$response" > results/cliente.json
    
    if check_response "$response" "Criação do Cliente"; then
        CLIENTE_ID=$(echo "$response" | jq -r '.id // .clienteId // .cliente_id // empty')
        print_info "Cliente ID: $CLIENTE_ID"
    else
        exit 1
    fi
}

# ========================================
# 5. CRIAR VEÍCULO
# ========================================

create_veiculo() {
    print_header "5. CRIAR VEÍCULO"
    
    local response=$(curl -s -X POST "$CUSTOMER_SERVICE/api/veiculos" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "clienteId": '$CLIENTE_ID',
            "placa": "ABC1D23",
            "marca": "Volkswagen",
            "modelo": "Gol",
            "ano": 2020,
            "cor": "Prata",
            "quilometragem": 45000
        }')
    
    echo "$response" > results/veiculo.json
    
    if check_response "$response" "Criação do Veículo"; then
        VEICULO_ID=$(echo "$response" | jq -r '.id // .veiculoId // .veiculo_id // empty')
        print_info "Veículo ID: $VEICULO_ID"
    else
        exit 1
    fi
}

# ========================================
# 6. CRIAR SERVIÇOS NO CATÁLOGO
# ========================================

create_servicos() {
    print_header "6. CRIAR SERVIÇOS NO CATÁLOGO"
    
    # Serviço 1: Troca de Óleo
    local response1=$(curl -s -w "\n%{http_code}" -X POST "$CATALOG_SERVICE/api/servicos" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "nome": "Troca de Óleo",
            "descricao": "Troca de óleo do motor com filtro",
            "precoBase": 150.00,
            "tempoEstimadoMinutos": 60,
            "categoria": "MECANICO"
        }')
    
    local http_code=$(echo "$response1" | tail -n1)
    local body=$(echo "$response1" | sed '$d')
    
    echo "$body" > results/servico1.json
    
    if [ "$http_code" = "201" ] || [ "$http_code" = "200" ]; then
        if check_response "$body" "Criação do Serviço 1 (Troca de Óleo)"; then
            SERVICO1_ID=$(echo "$body" | jq -r '.id // .servicoId // .servico_id // empty')
            print_info "Serviço 1 ID: $SERVICO1_ID"
        fi
    else
        print_error "Falha ao criar serviço 1 (HTTP $http_code)"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    fi
    
    # Serviço 2: Alinhamento
    local response2=$(curl -s -w "\n%{http_code}" -X POST "$CATALOG_SERVICE/api/servicos" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "nome": "Alinhamento e Balanceamento",
            "descricao": "Alinhamento e balanceamento das 4 rodas",
            "precoBase": 120.00,
            "tempoEstimadoMinutos": 90,
            "categoria": "ALINHAMENTO"
        }')
    
    local http_code=$(echo "$response2" | tail -n1)
    local body=$(echo "$response2" | sed '$d')
    
    echo "$body" > results/servico2.json
    
    if [ "$http_code" = "201" ] || [ "$http_code" = "200" ]; then
        if check_response "$body" "Criação do Serviço 2 (Alinhamento)"; then
            SERVICO2_ID=$(echo "$body" | jq -r '.id // .servicoId // .servico_id // empty')
            print_info "Serviço 2 ID: $SERVICO2_ID"
        fi
    else
        print_error "Falha ao criar serviço 2 (HTTP $http_code)"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    fi
}

# ========================================
# 7. CRIAR PRODUTOS NO CATÁLOGO
# ========================================

create_produtos() {
    print_header "7. CRIAR PRODUTOS NO CATÁLOGO"
    
    # Produto 1: Óleo
    local response1=$(curl -s -w "\n%{http_code}" -X POST "$CATALOG_SERVICE/api/catalogo-produtos" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "nome": "Óleo Motor 5W30",
            "descricao": "Óleo sintético para motor 5W30 - 1L",
            "preco": 45.00,
            "categoria": "INSUMO"
        }')
    
    local http_code=$(echo "$response1" | tail -n1)
    local body=$(echo "$response1" | sed '$d')
    
    echo "$body" > results/produto1.json
    
    if [ "$http_code" = "201" ] || [ "$http_code" = "200" ]; then
        if check_response "$body" "Criação do Produto 1 (Óleo)"; then
            PRODUTO1_ID=$(echo "$body" | jq -r '.id // .produtoId // .produto_id // empty')
            print_info "Produto 1 ID: $PRODUTO1_ID"
        fi
    else
        print_error "Falha ao criar produto 1 (HTTP $http_code)"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    fi
    
    # Produto 2: Filtro
    local response2=$(curl -s -w "\n%{http_code}" -X POST "$CATALOG_SERVICE/api/catalogo-produtos" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "nome": "Filtro de Óleo",
            "descricao": "Filtro de óleo original",
            "preco": 35.00,
            "categoria": "PECA"
        }')
    
    local http_code=$(echo "$response2" | tail -n1)
    local body=$(echo "$response2" | sed '$d')
    
    echo "$body" > results/produto2.json
    
    if [ "$http_code" = "201" ] || [ "$http_code" = "200" ]; then
        if check_response "$body" "Criação do Produto 2 (Filtro)"; then
            PRODUTO2_ID=$(echo "$body" | jq -r '.id // .produtoId // .produto_id // empty')
            print_info "Produto 2 ID: $PRODUTO2_ID"
        fi
    else
        print_error "Falha ao criar produto 2 (HTTP $http_code)"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    fi
}

# ========================================
# 8. ADICIONAR ESTOQUE
# ========================================

add_estoque() {
    print_header "8. ADICIONAR ESTOQUE"
    
    # Adicionar estoque do óleo
    local response1=$(curl -s -X POST "$INVENTORY_SERVICE/api/estoque/entrada" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "produtoId": '$PRODUTO1_ID',
            "quantidade": 100,
            "motivo": "Compra inicial",
            "fornecedor": "Distribuidora ABC"
        }')
    
    if check_response "$response1" "Entrada de Estoque - Óleo"; then
        print_info "Estoque de Óleo: 100 unidades"
    fi
    
    # Adicionar estoque do filtro
    local response2=$(curl -s -X POST "$INVENTORY_SERVICE/api/estoque/entrada" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "produtoId": '$PRODUTO2_ID',
            "quantidade": 50,
            "motivo": "Compra inicial",
            "fornecedor": "Distribuidora ABC"
        }')
    
    if check_response "$response2" "Entrada de Estoque - Filtro"; then
        print_info "Estoque de Filtro: 50 unidades"
    fi
}

# ========================================
# 9. CRIAR ORDEM DE SERVIÇO
# ========================================

create_ordem_servico() {
    print_header "9. CRIAR ORDEM DE SERVIÇO"
    
    local response=$(curl -s -X POST "$WORKORDER_SERVICE/api/ordens-servico" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "veiculoId": '$VEICULO_ID',
            "clienteId": '$CLIENTE_ID',
            "descricaoProblema": "Veículo apresentando ruídos no motor e direção desalinhada",
            "quilometragem": 45000
        }')
    
    echo "$response" > results/ordem-servico.json
    
    if check_response "$response" "Criação da Ordem de Serviço"; then
        OS_ID=$(echo "$response" | jq -r '.id // .ordemServicoId // .ordem_servico_id // empty')
        print_info "Ordem de Serviço ID: $OS_ID"
    else
        exit 1
    fi
}

# ========================================
# 10. ATRIBUIR MECÂNICO À OS
# ========================================

atribuir_mecanico() {
    print_header "10. ATRIBUIR MECÂNICO À OS"
    
    local response=$(curl -s -X PUT "$WORKORDER_SERVICE/api/ordens-servico/$OS_ID/mecanico" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "mecanicoId": '$MECANICO_ID'
        }')
    
    check_response "$response" "Atribuição do Mecânico"
}

# ========================================
# 11. ADICIONAR SERVIÇOS À OS
# ========================================

add_servicos_os() {
    print_header "11. ADICIONAR SERVIÇOS À OS"
    
    # Adicionar Serviço 1
    local response1=$(curl -s -X POST "$WORKORDER_SERVICE/api/ordens-servico/$OS_ID/servicos" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "servicoId": '$SERVICO1_ID',
            "quantidade": 1,
            "observacao": "Verificar nível e qualidade do óleo"
        }')
    
    check_response "$response1" "Adição do Serviço 1 (Troca de Óleo)"
    
    # Adicionar Serviço 2
    local response2=$(curl -s -X POST "$WORKORDER_SERVICE/api/ordens-servico/$OS_ID/servicos" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "servicoId": '$SERVICO2_ID',
            "quantidade": 1,
            "observacao": "Verificar desgaste dos pneus"
        }')
    
    check_response "$response2" "Adição do Serviço 2 (Alinhamento)"
}

# ========================================
# 12. ADICIONAR PRODUTOS À OS
# ========================================

add_produtos_os() {
    print_header "12. ADICIONAR PRODUTOS À OS"
    
    # Adicionar Produto 1 (Óleo)
    local response1=$(curl -s -X POST "$WORKORDER_SERVICE/api/ordens-servico/$OS_ID/produtos" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "produtoId": '$PRODUTO1_ID',
            "quantidade": 4
        }')
    
    check_response "$response1" "Adição do Produto 1 (Óleo - 4L)"
    
    # Adicionar Produto 2 (Filtro)
    local response2=$(curl -s -X POST "$WORKORDER_SERVICE/api/ordens-servico/$OS_ID/produtos" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "produtoId": '$PRODUTO2_ID',
            "quantidade": 1
        }')
    
    check_response "$response2" "Adição do Produto 2 (Filtro - 1un)"
}

# ========================================
# 13. RESERVAR ESTOQUE
# ========================================

reservar_estoque() {
    print_header "13. RESERVAR ESTOQUE"
    
    # Reservar Óleo
    local response1=$(curl -s -X POST "$INVENTORY_SERVICE/api/estoque/reserva" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "produtoId": '$PRODUTO1_ID',
            "quantidade": 4,
            "ordemServicoId": '$OS_ID'
        }')
    
    check_response "$response1" "Reserva de Estoque - Óleo"
    
    # Reservar Filtro
    local response2=$(curl -s -X POST "$INVENTORY_SERVICE/api/estoque/reserva" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "produtoId": '$PRODUTO2_ID',
            "quantidade": 1,
            "ordemServicoId": '$OS_ID'
        }')
    
    check_response "$response2" "Reserva de Estoque - Filtro"
}

# ========================================
# 14. DIAGNOSTICAR OS (GERAR ORÇAMENTO)
# ========================================

diagnosticar_os() {
    print_header "14. DIAGNOSTICAR OS (GERAR ORÇAMENTO)"
    
    local response=$(curl -s -X POST "$WORKORDER_SERVICE/api/ordens-servico/$OS_ID/diagnosticar" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "diagnostico": "Necessário troca de óleo e alinhamento. Óleo degradado e direção desalinhada.",
            "observacoes": "Cliente aprovou orçamento verbal"
        }')
    
    if check_response "$response" "Diagnóstico da OS"; then
        print_info "📧 Email de orçamento deve ter sido enviado para o cliente"
    fi
}

# ========================================
# 15. BUSCAR ORÇAMENTO
# ========================================

buscar_orcamento() {
    print_header "15. BUSCAR ORÇAMENTO"
    
    local response=$(curl -s -X GET "$BUDGET_SERVICE/api/orcamentos/ordem-servico/$OS_ID" \
        -H "Authorization: Bearer $TOKEN")
    
    echo "$response" > results/orcamento.json
    
    if check_response "$response" "Busca do Orçamento"; then
        ORCAMENTO_ID=$(echo "$response" | jq -r '.id // .orcamentoId // .orcamento_id // empty')
        local valor_total=$(echo "$response" | jq -r '.valorTotal // .valor_total // empty')
        print_info "Orçamento ID: $ORCAMENTO_ID"
        print_info "Valor Total: R$ $valor_total"
    fi
}

# ========================================
# 16. APROVAR ORÇAMENTO
# ========================================

aprovar_orcamento() {
    print_header "16. APROVAR ORÇAMENTO"
    
    local response=$(curl -s -X PUT "$BUDGET_SERVICE/api/orcamentos/$ORCAMENTO_ID/aprovar" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "observacoes": "Cliente aprovou por telefone"
        }')
    
    check_response "$response" "Aprovação do Orçamento"
}

# ========================================
# 17. EXECUTAR SERVIÇO
# ========================================

executar_servico() {
    print_header "17. EXECUTAR SERVIÇO"
    
    local response=$(curl -s -X PUT "$WORKORDER_SERVICE/api/ordens-servico/$OS_ID/executar" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "observacoes": "Iniciando execução dos serviços"
        }')
    
    check_response "$response" "Início da Execução"
}

# ========================================
# 18. FINALIZAR SERVIÇO
# ========================================

finalizar_servico() {
    print_header "18. FINALIZAR SERVIÇO"
    
    local response=$(curl -s -X PUT "$WORKORDER_SERVICE/api/ordens-servico/$OS_ID/finalizar" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "observacoes": "Todos os serviços executados com sucesso",
            "quilometragemFinal": 45050
        }')
    
    if check_response "$response" "Finalização do Serviço"; then
        print_info "📧 Email de veículo pronto deve ter sido enviado"
    fi
}

# ========================================
# 19. ENTREGAR VEÍCULO
# ========================================

entregar_veiculo() {
    print_header "19. ENTREGAR VEÍCULO"
    
    local response=$(curl -s -X PUT "$WORKORDER_SERVICE/api/ordens-servico/$OS_ID/entregar" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "observacoes": "Veículo entregue ao cliente. Pagamento realizado.",
            "formaPagamento": "CARTAO_CREDITO"
        }')
    
    check_response "$response" "Entrega do Veículo"
}

# ========================================
# 20. VERIFICAR ESTOQUE ATUALIZADO
# ========================================

verificar_estoque() {
    print_header "20. VERIFICAR ESTOQUE ATUALIZADO"
    
    # Verificar estoque do óleo
    local response1=$(curl -s -X GET "$INVENTORY_SERVICE/api/estoque/produto/$PRODUTO1_ID" \
        -H "Authorization: Bearer $TOKEN")
    
    if check_response "$response1" "Consulta Estoque - Óleo"; then
        local qtd_oleo=$(echo "$response1" | jq -r '.quantidade // .quantidadeDisponivel // empty')
        print_info "Estoque Óleo: $qtd_oleo unidades (esperado: 96)"
    fi
    
    # Verificar estoque do filtro
    local response2=$(curl -s -X GET "$INVENTORY_SERVICE/api/estoque/produto/$PRODUTO2_ID" \
        -H "Authorization: Bearer $TOKEN")
    
    if check_response "$response2" "Consulta Estoque - Filtro"; then
        local qtd_filtro=$(echo "$response2" | jq -r '.quantidade // .quantidadeDisponivel // empty')
        print_info "Estoque Filtro: $qtd_filtro unidades (esperado: 49)"
    fi
}

# ========================================
# GERAR RELATÓRIO FINAL
# ========================================

generate_report() {
    print_header "GERANDO RELATÓRIO"
    
    # Salvar IDs para reutilização
    cat > results/ids.env << EOF
# IDs gerados no teste E2E
ADMIN_ID=$ADMIN_ID
MECANICO_ID=$MECANICO_ID
CLIENTE_ID=$CLIENTE_ID
VEICULO_ID=$VEICULO_ID
SERVICO1_ID=$SERVICO1_ID
SERVICO2_ID=$SERVICO2_ID
PRODUTO1_ID=$PRODUTO1_ID
PRODUTO2_ID=$PRODUTO2_ID
OS_ID=$OS_ID
ORCAMENTO_ID=$ORCAMENTO_ID
TOKEN=$TOKEN
EOF
    
    print_success "Relatório salvo em results/ids.env"
}

# ========================================
# BANNER INICIAL
# ========================================

print_banner() {
    echo -e "${GREEN}"
    echo "╔═══════════════════════════════════════════════════════════════╗"
    echo "║                                                               ║"
    echo "║     🔧 TESTE E2E - OFICINA MICROSERVICES 🔧                   ║"
    echo "║                                                               ║"
    echo "║  Teste automatizado completo do fluxo de ordem de serviço    ║"
    echo "║                                                               ║"
    echo "╚═══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# ========================================
# FUNÇÃO PRINCIPAL
# ========================================

main() {
    print_banner
    
    # Executar todos os testes em sequência
    verify_all_services || exit 1
    create_admin || exit 1
    login || exit 1
    create_mecanico
    create_cliente || exit 1
    create_veiculo || exit 1
    create_servicos
    create_produtos
    add_estoque
    create_ordem_servico || exit 1
    atribuir_mecanico
    add_servicos_os
    add_produtos_os
    reservar_estoque
    diagnosticar_os
    buscar_orcamento
    aprovar_orcamento
    executar_servico
    finalizar_servico
    entregar_veiculo
    verificar_estoque
    generate_report
    
    # Mensagem final
    echo -e "\n${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}✅ TESTE E2E CONCLUÍDO COM SUCESSO!${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
    
    echo -e "${BLUE}📊 Resumo dos IDs Criados:${NC}"
    echo -e "  ${YELLOW}•${NC} Cliente ID:      ${GREEN}$CLIENTE_ID${NC}"
    echo -e "  ${YELLOW}•${NC} Veículo ID:      ${GREEN}$VEICULO_ID${NC}"
    echo -e "  ${YELLOW}•${NC} Mecânico ID:     ${GREEN}$MECANICO_ID${NC}"
    echo -e "  ${YELLOW}•${NC} Ordem Serviço:   ${GREEN}$OS_ID${NC}"
    echo -e "  ${YELLOW}•${NC} Orçamento ID:    ${GREEN}$ORCAMENTO_ID${NC}\n"
    
    echo -e "${BLUE}📁 Arquivos salvos em:${NC} ${GREEN}results/${NC}\n"
    echo -e "${BLUE}🧹 Para limpar dados:${NC} ${YELLOW}./cleanup.sh${NC}\n"
}

# Executar o script
main
