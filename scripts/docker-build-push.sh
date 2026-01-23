#!/bin/bash

################################################################################
# Script para Build e Push de Imagens Docker para Docker Hub
# Suporta tags: develop, main e latest
#
# Uso:
#   ./docker-build-push.sh <tag>
#
# Exemplos:
#   ./docker-build-push.sh develop
#   ./docker-build-push.sh main
#   ./docker-build-push.sh latest
################################################################################

set -e  # Para na primeira falha

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configurações
REGISTRY="${DOCKER_REGISTRY:-}"  # Ex: seu-usuario
TAG="${1:-develop}"              # Tag padrão: develop
PLATFORMS="linux/amd64,linux/arm64"

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

################################################################################
# Funções Auxiliares
################################################################################

print_header() {
    echo -e "\n${CYAN}========================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}========================================${NC}\n"
}

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker não está instalado ou não está no PATH"
        exit 1
    fi
    print_success "Docker encontrado: $(docker --version)"
}

check_registry() {
    if [ -z "$REGISTRY" ]; then
        print_error "DOCKER_REGISTRY não está definido!"
        echo ""
        echo "Configure a variável de ambiente DOCKER_REGISTRY com seu usuário do Docker Hub:"
        echo "  export DOCKER_REGISTRY=seu-usuario"
        echo ""
        echo "Ou passe como argumento:"
        echo "  DOCKER_REGISTRY=seu-usuario ./docker-build-push.sh $TAG"
        exit 1
    fi
    print_success "Registry: $REGISTRY"
}

docker_login() {
    print_info "Verificando autenticação Docker Hub..."

    if ! docker info | grep -q "Username"; then
        print_warning "Não autenticado no Docker Hub"
        print_info "Fazendo login no Docker Hub..."
        docker login
    else
        print_success "Já autenticado no Docker Hub"
    fi
}

build_maven() {
    print_header "MAVEN BUILD - Compilando Todos os Serviços"

    print_info "Limpando e compilando projeto Maven..."
    mvn clean install -DskipTests

    print_success "Build Maven completo!"
}

build_and_push_service() {
    local service=$1
    local image_name="${REGISTRY}/${service}"

    print_header "BUILD & PUSH: ${service}"

    # Build da imagem
    print_info "Building ${image_name}:${TAG}..."
    docker build \
        -t "${image_name}:${TAG}" \
        -f "./${service}/Dockerfile" \
        .

    print_success "Imagem ${image_name}:${TAG} criada!"

    # Se for tag main, também marca como latest
    if [ "$TAG" = "main" ]; then
        print_info "Tag 'main' detectada - criando tag 'latest'..."
        docker tag "${image_name}:${TAG}" "${image_name}:latest"
        print_success "Tag 'latest' criada"
    fi

    # Push da imagem com a tag especificada
    print_info "Pushing ${image_name}:${TAG}..."
    docker push "${image_name}:${TAG}"
    print_success "Pushed ${image_name}:${TAG}"

    # Push da tag latest se for main
    if [ "$TAG" = "main" ]; then
        print_info "Pushing ${image_name}:latest..."
        docker push "${image_name}:latest"
        print_success "Pushed ${image_name}:latest"
    fi
}

build_multiarch_and_push_service() {
    local service=$1
    local image_name="${REGISTRY}/${service}"

    print_header "BUILD MULTI-ARCH & PUSH: ${service}"

    # Build multi-arch e push direto
    print_info "Building multi-arch (${PLATFORMS}) ${image_name}:${TAG}..."

    if [ "$TAG" = "main" ]; then
        # Se for main, cria tags main e latest
        docker buildx build \
            --platform "${PLATFORMS}" \
            --push \
            -t "${image_name}:${TAG}" \
            -t "${image_name}:latest" \
            -f "./${service}/Dockerfile" \
            .
        print_success "Pushed ${image_name}:${TAG} e ${image_name}:latest (multi-arch)"
    else
        # Caso contrário, só a tag especificada
        docker buildx build \
            --platform "${PLATFORMS}" \
            --push \
            -t "${image_name}:${TAG}" \
            -f "./${service}/Dockerfile" \
            .
        print_success "Pushed ${image_name}:${TAG} (multi-arch)"
    fi
}

print_summary() {
    print_header "RESUMO DO BUILD E PUSH"

    echo -e "${GREEN}✓ Todos os serviços foram construídos e enviados com sucesso!${NC}"
    echo ""
    echo "Registry: ${REGISTRY}"
    echo "Tag: ${TAG}"
    echo ""
    echo "Imagens criadas:"
    for service in "${SERVICES[@]}"; do
        echo "  - ${REGISTRY}/${service}:${TAG}"
        if [ "$TAG" = "main" ]; then
            echo "  - ${REGISTRY}/${service}:latest"
        fi
    done
    echo ""
    echo "Para usar as imagens em produção, configure no docker-compose.prod.yml:"
    echo "  image: ${REGISTRY}/<service>:${TAG}"
}

show_usage() {
    cat << EOF
${CYAN}Build e Push de Imagens Docker para Docker Hub${NC}

${YELLOW}Uso:${NC}
  ./docker-build-push.sh [TAG] [OPÇÕES]

${YELLOW}Argumentos:${NC}
  TAG         Tag da imagem (padrão: develop)
              Opções: develop, main, latest ou qualquer tag customizada

${YELLOW}Variáveis de Ambiente:${NC}
  DOCKER_REGISTRY    Usuário do Docker Hub (obrigatório)
  MULTIARCH          Build multi-arquitetura (amd64+arm64) - padrão: false
                     Use: MULTIARCH=true ./docker-build-push.sh

${YELLOW}Exemplos:${NC}
  # Build e push com tag develop
  DOCKER_REGISTRY=meuusuario ./docker-build-push.sh develop

  # Build e push com tag main (cria também tag latest)
  DOCKER_REGISTRY=meuusuario ./docker-build-push.sh main

  # Build multi-arquitetura
  DOCKER_REGISTRY=meuusuario MULTIARCH=true ./docker-build-push.sh main

  # Exportar registry uma vez e usar várias vezes
  export DOCKER_REGISTRY=meuusuario
  ./docker-build-push.sh develop
  ./docker-build-push.sh main

${YELLOW}Notas:${NC}
  - Tag 'main' cria automaticamente tag 'latest'
  - Requer autenticação no Docker Hub (docker login)
  - Multi-arch requer Docker Buildx configurado

EOF
}

################################################################################
# Main
################################################################################

main() {
    # Mostra ajuda se solicitado
    if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_usage
        exit 0
    fi

    print_header "DOCKER BUILD & PUSH - Oficina Microservices"

    print_info "Tag: ${TAG}"
    print_info "Multi-arch: ${MULTIARCH:-false}"

    # Verificações iniciais
    check_docker
    check_registry
    docker_login

    # Build Maven
    build_maven

    # Build e push de cada serviço
    print_header "PROCESSANDO ${#SERVICES[@]} SERVIÇOS"

    local count=0
    for service in "${SERVICES[@]}"; do
        count=$((count + 1))
        echo -e "\n${YELLOW}[${count}/${#SERVICES[@]}]${NC} Processando ${service}...\n"

        if [ "${MULTIARCH:-false}" = "true" ]; then
            build_multiarch_and_push_service "$service"
        else
            build_and_push_service "$service"
        fi
    done

    # Resumo final
    print_summary

    print_success "PROCESSO COMPLETO!"
}

# Executa main
main "$@"
