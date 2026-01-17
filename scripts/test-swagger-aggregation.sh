#!/bin/bash

# Script para testar o Swagger Aggregator do Eureka Server
# Uso: ./scripts/test-swagger-aggregation.sh [eureka-url]

set -e

# Configuração
EUREKA_URL="${1:-http://localhost:8761}"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "======================================"
echo "  Swagger Aggregation Test"
echo "======================================"
echo ""
echo "Eureka URL: $EUREKA_URL"
echo ""

# Verificar se jq está instalado
if ! command -v jq &> /dev/null; then
    echo -e "${RED}✗ jq is not installed${NC}"
    echo "Install jq to parse JSON responses:"
    echo "  macOS: brew install jq"
    echo "  Ubuntu: sudo apt-get install jq"
    exit 1
fi

# Função para testar endpoint
test_endpoint() {
    local name=$1
    local endpoint=$2
    local expected_status=${3:-200}

    echo -n "Testing $name... "

    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$EUREKA_URL$endpoint" 2>/dev/null || echo "000")

    if [ "$HTTP_CODE" = "$expected_status" ]; then
        echo -e "${GREEN}✓ OK${NC} (HTTP $HTTP_CODE)"
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $HTTP_CODE, expected $expected_status)"
        return 1
    fi
}

echo "======================================"
echo "1. Testing Eureka Server"
echo "======================================"
echo ""

if ! test_endpoint "Eureka Server Health" "/actuator/health"; then
    echo -e "${RED}✗ Eureka Server is not reachable${NC}"
    echo "Make sure Eureka Server is running at $EUREKA_URL"
    exit 1
fi

test_endpoint "Eureka Dashboard" "/" || true

echo ""
echo "======================================"
echo "2. Testing Swagger UI Endpoints"
echo "======================================"
echo ""

if test_endpoint "Swagger UI" "/swagger-ui.html"; then
    SWAGGER_UI_WORKING=true
else
    SWAGGER_UI_WORKING=false
fi

if test_endpoint "Swagger Config" "/v3/api-docs/swagger-config"; then
    SWAGGER_CONFIG_WORKING=true
else
    SWAGGER_CONFIG_WORKING=false
fi

echo ""
echo "======================================"
echo "3. Checking Swagger Configuration"
echo "======================================"
echo ""

if [ "$SWAGGER_CONFIG_WORKING" = true ]; then
    CONFIG=$(curl -s "$EUREKA_URL/v3/api-docs/swagger-config" 2>/dev/null)
    URLS_COUNT=$(echo "$CONFIG" | jq '.urls | length // 0' 2>/dev/null)

    echo "URLs configured in Swagger: $URLS_COUNT"
    echo ""

    if [ "$URLS_COUNT" -gt 0 ]; then
        echo "Services in dropdown:"
        echo "$CONFIG" | jq -r '.urls[] | "\(.name): \(.url)"' 2>/dev/null | while read line; do
            echo -e "  ${BLUE}•${NC} $line"
        done
    else
        echo -e "${YELLOW}⚠${NC} No URLs configured in Swagger."
        echo "Check application.yml for springdoc.swagger-ui.urls configuration"
    fi
else
    echo -e "${RED}✗ Cannot retrieve Swagger configuration${NC}"
    URLS_COUNT=0
fi

echo ""
echo "======================================"
echo "4. Testing Individual Service Docs"
echo "======================================"
echo ""

if [ "$SWAGGER_CONFIG_WORKING" = true ] && [ "$URLS_COUNT" -gt 0 ]; then
    REACHABLE=0
    UNREACHABLE=0

    echo "Checking API docs endpoints:"
    echo ""

    echo "$CONFIG" | jq -r '.urls[] | "\(.name)|\(.url)"' 2>/dev/null | while read line; do
        service_name=$(echo "$line" | cut -d'|' -f1)
        url=$(echo "$line" | cut -d'|' -f2)

        echo -n "  $service_name... "

        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")

        if [ "$HTTP_CODE" = "200" ]; then
            echo -e "${GREEN}✓ OK${NC} (HTTP $HTTP_CODE)"
            ((REACHABLE++)) || true
        else
            echo -e "${RED}✗ UNREACHABLE${NC} (HTTP $HTTP_CODE)"
            ((UNREACHABLE++)) || true
        fi
    done

    echo ""
    echo -e "Summary: ${GREEN}$REACHABLE reachable${NC}, ${RED}$UNREACHABLE unreachable${NC}"
else
    echo -e "${YELLOW}⚠${NC} Skipping - Swagger configuration not available"
fi

echo ""
echo "======================================"
echo "5. Checking Eureka Registered Services"
echo "======================================"
echo ""

echo "Checking services registered with Eureka..."
echo ""

EUREKA_APPS=$(curl -s "$EUREKA_URL/eureka/apps" -H "Accept: application/json" 2>/dev/null)
REGISTERED_COUNT=$(echo "$EUREKA_APPS" | jq '.applications.application | length // 0' 2>/dev/null)

if [ "$REGISTERED_COUNT" -gt 0 ]; then
    echo "Services registered: $REGISTERED_COUNT"
    echo ""
    echo "$EUREKA_APPS" | jq -r '.applications.application[].app // empty' 2>/dev/null | sort | while read app; do
        STATUS=$(echo "$EUREKA_APPS" | jq -r ".applications.application[] | select(.app==\"$app\") | .instance[0].status" 2>/dev/null)
        if [ "$STATUS" = "UP" ]; then
            echo -e "  ${GREEN}✓${NC} $app (UP)"
        else
            echo -e "  ${RED}✗${NC} $app ($STATUS)"
        fi
    done
else
    echo -e "${YELLOW}⚠${NC} No services registered with Eureka yet"
    echo ""
    echo "Services need to be running and registered before appearing in Swagger"
fi

echo ""
echo "======================================"
echo "Summary"
echo "======================================"
echo ""

ALL_GOOD=true

if [ "$SWAGGER_UI_WORKING" = true ]; then
    echo -e "${GREEN}✓ Swagger UI is accessible${NC}"
else
    echo -e "${RED}✗ Swagger UI is NOT accessible${NC}"
    ALL_GOOD=false
fi

if [ "$SWAGGER_CONFIG_WORKING" = true ]; then
    echo -e "${GREEN}✓ Swagger Config is accessible${NC}"
else
    echo -e "${RED}✗ Swagger Config is NOT accessible${NC}"
    ALL_GOOD=false
fi

if [ "$URLS_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ Swagger has $URLS_COUNT service(s) configured${NC}"
else
    echo -e "${YELLOW}⚠ No services configured in Swagger${NC}"
    ALL_GOOD=false
fi

if [ "$REGISTERED_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ Eureka has $REGISTERED_COUNT service(s) registered${NC}"
else
    echo -e "${YELLOW}⚠ No services registered with Eureka${NC}"
fi

echo ""

if [ "$ALL_GOOD" = true ] && [ "$URLS_COUNT" -gt 0 ]; then
    echo -e "${GREEN}════════════════════════════════════════${NC}"
    echo -e "${GREEN}✓ Swagger Aggregation is working!${NC}"
    echo -e "${GREEN}════════════════════════════════════════${NC}"
    echo ""
    echo "Access the unified Swagger UI at:"
    echo -e "  ${BLUE}👉 $EUREKA_URL/swagger-ui.html${NC}"
    echo ""
    echo "Steps to use:"
    echo "  1. Open the URL above in your browser"
    echo "  2. Select a service from the dropdown at the top"
    echo "  3. Explore and test the API endpoints"
elif [ "$SWAGGER_UI_WORKING" = true ] && [ "$URLS_COUNT" = 0 ]; then
    echo -e "${YELLOW}════════════════════════════════════════${NC}"
    echo -e "${YELLOW}⚠ Swagger is configured but no services${NC}"
    echo -e "${YELLOW}════════════════════════════════════════${NC}"
    echo ""
    echo "The Swagger aggregation is set up, but no services are configured."
    echo ""
    echo "Check configuration in:"
    echo "  eureka-server/src/main/resources/application.yml"
    echo "  eureka-server/src/main/resources/application-docker.yml"
    echo "  eureka-server/src/main/resources/application-k8s.yml"
elif [ "$REGISTERED_COUNT" = 0 ]; then
    echo -e "${YELLOW}════════════════════════════════════════${NC}"
    echo -e "${YELLOW}⚠ No services are running${NC}"
    echo -e "${YELLOW}════════════════════════════════════════${NC}"
    echo ""
    echo "Eureka Server is up but no microservices are registered."
    echo ""
    echo "To start services:"
    echo ""
    echo "  # Using Docker Compose (recommended)"
    echo "  docker compose --profile dev up -d"
    echo ""
    echo "  # Or individually (local development)"
    echo "  cd auth-service && mvn spring-boot:run"
    echo "  cd customer-service && mvn spring-boot:run"
    echo "  cd catalog-service && mvn spring-boot:run"
    echo "  # ... etc"
else
    echo -e "${RED}════════════════════════════════════════${NC}"
    echo -e "${RED}✗ Swagger Aggregation has issues${NC}"
    echo -e "${RED}════════════════════════════════════════${NC}"
    echo ""
    echo "Review the errors above and fix configuration."
fi

echo ""
