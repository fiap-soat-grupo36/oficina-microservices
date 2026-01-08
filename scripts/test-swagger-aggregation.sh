#!/bin/bash

# Script para testar o Swagger Aggregator do Eureka Server
# Uso: ./scripts/test-swagger-aggregation.sh [eureka-url]

set -e

# Configuração
EUREKA_URL="${1:-http://localhost:8761}"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "  Swagger Aggregation Test"
echo "======================================"
echo ""
echo "Eureka URL: $EUREKA_URL"
echo ""

# Função para testar endpoint
test_endpoint() {
    local name=$1
    local endpoint=$2
    local expected_status=${3:-200}

    echo -n "Testing $name... "

    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$EUREKA_URL$endpoint")

    if [ "$HTTP_CODE" = "$expected_status" ]; then
        echo -e "${GREEN}✓ OK${NC} (HTTP $HTTP_CODE)"
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $HTTP_CODE, expected $expected_status)"
        return 1
    fi
}

# Função para verificar JSON
check_json() {
    local name=$1
    local endpoint=$2
    local jq_filter=$3

    echo "Checking $name..."

    RESPONSE=$(curl -s "$EUREKA_URL$endpoint")
    RESULT=$(echo "$RESPONSE" | jq -r "$jq_filter" 2>/dev/null)

    if [ -n "$RESULT" ] && [ "$RESULT" != "null" ]; then
        echo -e "${GREEN}✓${NC} $RESULT"
        return 0
    else
        echo -e "${RED}✗ Failed to parse response${NC}"
        echo "Response: $RESPONSE"
        return 1
    fi
}

echo "======================================"
echo "1. Testing Swagger UI Endpoints"
echo "======================================"
echo ""

test_endpoint "Swagger UI" "/swagger-ui.html" || true
test_endpoint "Swagger Config" "/v3/api-docs/swagger-config" || true
test_endpoint "Health Check" "/swagger/health" || true
test_endpoint "Services List" "/swagger/services" || true

echo ""
echo "======================================"
echo "2. Checking Discovered Services"
echo "======================================"
echo ""

check_json "Total Services" "/swagger/health" ".servicesDiscovered"
echo ""

echo "Services discovered:"
SERVICES=$(curl -s "$EUREKA_URL/swagger/health" | jq -r '.services[]' 2>/dev/null)
if [ -n "$SERVICES" ]; then
    echo "$SERVICES" | while read service; do
        echo -e "  ${GREEN}•${NC} $service"
    done
else
    echo -e "${YELLOW}⚠${NC} No services discovered yet"
fi

echo ""
echo "======================================"
echo "3. Checking Service URLs"
echo "======================================"
echo ""

SERVICES_JSON=$(curl -s "$EUREKA_URL/swagger/services")
TOTAL=$(echo "$SERVICES_JSON" | jq -r '.totalServices // 0')

echo "Total services with API docs: $TOTAL"
echo ""

if [ "$TOTAL" -gt 0 ]; then
    echo "$SERVICES_JSON" | jq -r '.services[] | "\(.displayName): \(.apiDocsUrl)"' | while read line; do
        echo -e "  ${GREEN}•${NC} $line"
    done
else
    echo -e "${YELLOW}⚠${NC} No services registered yet. Make sure microservices are running."
fi

echo ""
echo "======================================"
echo "4. Testing Swagger Config"
echo "======================================"
echo ""

CONFIG=$(curl -s "$EUREKA_URL/v3/api-docs/swagger-config")
URLS_COUNT=$(echo "$CONFIG" | jq '.urls | length // 0')

echo "URLs configured in Swagger: $URLS_COUNT"
echo ""

if [ "$URLS_COUNT" -gt 0 ]; then
    echo "$CONFIG" | jq -r '.urls[] | "\(.name): \(.url)"' | while read line; do
        echo -e "  ${GREEN}•${NC} $line"
    done
else
    echo -e "${YELLOW}⚠${NC} No URLs configured. Services may not be ready."
fi

echo ""
echo "======================================"
echo "5. Testing Individual Service Docs"
echo "======================================"
echo ""

if [ "$URLS_COUNT" -gt 0 ]; then
    echo "$CONFIG" | jq -r '.urls[] | .url' | while read url; do
        service_name=$(echo "$url" | cut -d'/' -f3 | cut -d':' -f1)
        echo -n "  Checking $service_name API docs... "

        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")

        if [ "$HTTP_CODE" = "200" ]; then
            echo -e "${GREEN}✓${NC}"
        else
            echo -e "${RED}✗ (HTTP $HTTP_CODE)${NC}"
        fi
    done
else
    echo -e "${YELLOW}⚠${NC} Skipping - no services to test"
fi

echo ""
echo "======================================"
echo "Summary"
echo "======================================"
echo ""

if [ "$TOTAL" -gt 0 ] && [ "$URLS_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ Swagger Aggregation is working!${NC}"
    echo ""
    echo "Access the unified Swagger UI at:"
    echo "  👉 $EUREKA_URL/swagger-ui.html"
else
    echo -e "${YELLOW}⚠ Swagger Aggregation is configured but no services are available.${NC}"
    echo ""
    echo "Make sure microservices are running and registered with Eureka."
    echo ""
    echo "To start services locally:"
    echo "  docker compose --profile dev up -d"
    echo ""
    echo "Or individually:"
    echo "  cd <service-name> && mvn spring-boot:run"
fi

echo ""
