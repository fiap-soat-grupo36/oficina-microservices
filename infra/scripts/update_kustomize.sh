#!/bin/bash
set -e

# Este script espera a tag da imagem como primeiro argumento
# e o caminho para o kustomize overlay como segundo argumento.
IMAGE_TAG=$1
KUSTOMIZE_PATH=$2

if [ -z "$IMAGE_TAG" ] || [ -z "$KUSTOMIZE_PATH" ]; then
  echo "Usage: $0 <image-tag> <kustomize-path>"
  exit 1
fi

# Navega para o diretório do kustomize
cd "$KUSTOMIZE_PATH"

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

# Itera sobre os serviços e define a imagem para cada um
for SERVICE in "${SERVICES[@]}"; do
  kustomize edit set image "grecomilani/${SERVICE}=grecomilani/${SERVICE}:${IMAGE_TAG}"
done

# Gera o manifesto final para o stdout, que será capturado pelo Terraform
kustomize build .
