#!/bin/bash

set -e

# Configurações
DOCKER_HUB_USERNAME="${DOCKER_HUB_USERNAME:-grecomilani}"
TAG="${TAG:-latest}"

# Lista de serviços
SERVICES=(
    "eureka-server"
    "auth-service"
    "customer-service"
    "catalog-service"
    "inventory-service"
    "budget-service"
    "work-order-service"
    "notification-service"
)

echo "=============================================="
echo "  Build and Push Docker Images"
echo "  Docker Hub: ${DOCKER_HUB_USERNAME}"
echo "  Tag: ${TAG}"
echo "=============================================="

# Navegar para o diretório raiz do projeto
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo ""
echo "==> Navegando para: ${PROJECT_ROOT}"
cd "${PROJECT_ROOT}"

# Verificar se estamos no diretório correto
if [ ! -f "pom.xml" ]; then
    echo "ERRO: Não foi possível encontrar o pom.xml do projeto"
    exit 1
fi

# Login no Docker Hub
echo ""
echo "==> Fazendo login no Docker Hub..."
docker login

# Build e push de cada serviço
for service in "${SERVICES[@]}"; do
    echo ""
    echo "========================================="
    echo "Processando: ${service}"
    echo "========================================="

    image_name="${DOCKER_HUB_USERNAME}/${service}:${TAG}"

    echo "Building ${image_name}..."
    docker build -t "${image_name}" -f "${service}/Dockerfile" .

    echo "Pushing ${image_name}..."
    docker push "${image_name}"

    echo "✓ ${service} concluído"
done

echo ""
echo "=============================================="
echo "  TODAS AS IMAGENS FORAM CRIADAS E ENVIADAS!"
echo "=============================================="
