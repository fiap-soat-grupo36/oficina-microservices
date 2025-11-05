#!/bin/bash

source config.env
source results/ids.env 2>/dev/null

print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_header "LIMPANDO DADOS DE TESTE"

# Deletar OS
if [ ! -z "$OS_ID" ]; then
    curl -s -X DELETE "$WORKORDER_SERVICE/api/ordens-servico/$OS_ID" \
        -H "Authorization: Bearer $TOKEN"
    print_success "Ordem de Serviço deletada"
fi

# Deletar Veículo
if [ ! -z "$VEICULO_ID" ]; then
    curl -s -X DELETE "$CUSTOMER_SERVICE/api/veiculos/$VEICULO_ID" \
        -H "Authorization: Bearer $TOKEN"
    print_success "Veículo deletado"
fi

# Deletar Cliente
if [ ! -z "$CLIENTE_ID" ]; then
    curl -s -X DELETE "$CUSTOMER_SERVICE/api/clientes/$CLIENTE_ID" \
        -H "Authorization: Bearer $TOKEN"
    print_success "Cliente deletado"
fi

# Limpar diretório de resultados
rm -rf results/*
print_success "Arquivos de resultado limpos"

echo -e "\n${GREEN}✅ Limpeza concluída!${NC}\n"
