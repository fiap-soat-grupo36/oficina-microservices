#!/bin/bash

# Script para baixar todas as especificações OpenAPI (v3/api-docs) dos microservices
# Uso: ./scripts/download-api-docs.sh [output-dir]

set -e

# Configuração
OUTPUT_DIR="${1:-./api-docs}"
EUREKA_URL="${EUREKA_URL:-http://localhost:8761}"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "  Download OpenAPI Specifications"
echo "======================================"
echo ""
echo "Eureka URL: $EUREKA_URL"
echo "Output directory: $OUTPUT_DIR"
echo ""

# Criar diretório de saída
mkdir -p "$OUTPUT_DIR"

# Verificar se jq está instalado
if ! command -v jq &> /dev/null; then
    echo -e "${RED}✗ jq is not installed${NC}"
    echo "Install jq to parse JSON responses:"
    echo "  macOS: brew install jq"
    echo "  Ubuntu: sudo apt-get install jq"
    exit 1
fi

# Obter configuração do Swagger
echo "Fetching Swagger configuration..."
CONFIG=$(curl -s "$EUREKA_URL/v3/api-docs/swagger-config" 2>/dev/null)

if [ -z "$CONFIG" ]; then
    echo -e "${RED}✗ Failed to fetch Swagger configuration${NC}"
    echo "Make sure Eureka Server is accessible at $EUREKA_URL"
    exit 1
fi

URLS_COUNT=$(echo "$CONFIG" | jq '.urls | length // 0' 2>/dev/null)

if [ "$URLS_COUNT" -eq 0 ]; then
    echo -e "${YELLOW}⚠ No services configured in Swagger${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Found $URLS_COUNT services${NC}"
echo ""

# Baixar cada especificação
echo "Downloading API docs..."
echo ""

SUCCESS=0
FAILED=0

echo "$CONFIG" | jq -r '.urls[] | "\(.name)|\(.url)"' 2>/dev/null | while IFS='|' read -r name url; do
    # Criar nome de arquivo seguro (remover espaços e caracteres especiais)
    filename=$(echo "$name" | tr '[:upper:]' '[:lower:]' | sed 's/ /-/g' | sed 's/[^a-z0-9-]//g')
    output_file="$OUTPUT_DIR/${filename}.json"

    echo -n "  ${BLUE}$name${NC}... "

    # Baixar especificação
    HTTP_CODE=$(curl -s -o "$output_file" -w "%{http_code}" "$url" 2>/dev/null)

    if [ "$HTTP_CODE" = "200" ]; then
        # Verificar se é JSON válido
        if jq empty "$output_file" 2>/dev/null; then
            FILE_SIZE=$(ls -lh "$output_file" | awk '{print $5}')
            echo -e "${GREEN}✓ OK${NC} (${FILE_SIZE})"
            ((SUCCESS++)) || true
        else
            echo -e "${RED}✗ Invalid JSON${NC}"
            rm "$output_file"
            ((FAILED++)) || true
        fi
    else
        echo -e "${RED}✗ HTTP $HTTP_CODE${NC}"
        rm -f "$output_file"
        ((FAILED++)) || true
    fi
done

# Aguardar subshell terminar
wait

# Contar arquivos baixados
DOWNLOADED=$(find "$OUTPUT_DIR" -name "*.json" -type f | wc -l | tr -d ' ')

echo ""
echo "======================================"
echo "Summary"
echo "======================================"
echo ""

if [ "$DOWNLOADED" -gt 0 ]; then
    echo -e "${GREEN}✓ Successfully downloaded $DOWNLOADED specification(s)${NC}"
    echo ""
    echo "Files saved in: $OUTPUT_DIR"
    echo ""
    echo "Downloaded files:"
    ls -lh "$OUTPUT_DIR"/*.json 2>/dev/null | awk '{printf "  • %s (%s)\n", $9, $5}'
    echo ""
    echo "Usage examples:"
    echo ""
    echo "  # View specification"
    echo "  cat $OUTPUT_DIR/auth-service.json | jq ."
    echo ""
    echo "  # Extract all endpoints"
    echo "  cat $OUTPUT_DIR/auth-service.json | jq '.paths | keys[]'"
    echo ""
    echo "  # Generate client code (requires openapi-generator)"
    echo "  openapi-generator-cli generate -i $OUTPUT_DIR/auth-service.json -g java -o ./generated-client"
    echo ""
else
    echo -e "${RED}✗ No specifications downloaded${NC}"
    echo ""
    echo "Check if services are accessible and configured correctly."
    exit 1
fi
