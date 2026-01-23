#!/bin/bash

################################################################################
# Script Simplificado - Build & Push para Develop e Main
#
# Este script faz build e push de AMBAS as tags: develop e main
################################################################################

set -e

# Cores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}BUILD & PUSH: develop + main${NC}"
echo -e "${CYAN}========================================${NC}\n"

# Verifica se DOCKER_REGISTRY está definido
if [ -z "$DOCKER_REGISTRY" ]; then
    echo -e "${BLUE}DOCKER_REGISTRY não definido.${NC}"
    read -p "Digite seu usuário do Docker Hub: " registry
    export DOCKER_REGISTRY=$registry
fi

echo -e "${GREEN}Registry: $DOCKER_REGISTRY${NC}\n"

# Pergunta qual tag fazer
echo "Escolha a opção:"
echo "  1) Build e push apenas DEVELOP"
echo "  2) Build e push apenas MAIN (+ latest)"
echo "  3) Build e push AMBAS (develop + main)"
echo ""
read -p "Opção [1-3]: " option

case $option in
    1)
        echo -e "\n${BLUE}==> Executando build para DEVELOP...${NC}\n"
        ./docker-build-push.sh develop
        ;;
    2)
        echo -e "\n${BLUE}==> Executando build para MAIN...${NC}\n"
        ./docker-build-push.sh main
        ;;
    3)
        echo -e "\n${BLUE}==> Executando build para DEVELOP...${NC}\n"
        ./docker-build-push.sh develop

        echo -e "\n${BLUE}==> Executando build para MAIN...${NC}\n"
        ./docker-build-push.sh main
        ;;
    *)
        echo "Opção inválida!"
        exit 1
        ;;
esac

echo -e "\n${GREEN}✓ PROCESSO COMPLETO!${NC}\n"
