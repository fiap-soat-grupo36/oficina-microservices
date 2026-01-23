#!/bin/bash

# Script para redeploy manual dos microserviços no EKS
# Autor: Script gerado para oficina-microservices
# Data: $(date +%Y-%m-%d)

set -e  # Para em caso de erro

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurações
NAMESPACE="${NAMESPACE:-oficina}"
CONTEXT="${CONTEXT:-}"
TAG="${TAG:-latest}"
DOCKER_HUB_USERNAME="${DOCKER_HUB_USERNAME:-grecomilani}"

# Lista de deployments
DEPLOYMENTS=(
    "eureka-server"
    "auth-service"
    "customer-service"
    "catalog-service"
    "inventory-service"
    "budget-service"
    "work-order-service"
    "notification-service"
)

# Função para exibir mensagens
print_step() {
    echo -e "${BLUE}==>${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Função para verificar se kubectl está instalado
check_kubectl() {
    print_step "Verificando se kubectl está instalado..."
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl não está instalado. Por favor, instale o kubectl."
        exit 1
    fi
    print_success "kubectl encontrado: $(kubectl version --client --short 2>/dev/null || kubectl version --client)"
}

# Função para verificar conectividade com cluster
check_cluster_connection() {
    print_step "Verificando conexão com cluster EKS..."

    if [ -n "${CONTEXT}" ]; then
        print_step "Usando contexto: ${CONTEXT}"
        kubectl config use-context "${CONTEXT}"
    fi

    if ! kubectl cluster-info &> /dev/null; then
        print_error "Não foi possível conectar ao cluster. Verifique sua configuração do kubectl."
        print_warning "Execute: aws eks update-kubeconfig --region <region> --name <cluster-name>"
        exit 1
    fi

    local current_context=$(kubectl config current-context)
    print_success "Conectado ao cluster: ${current_context}"
}

# Função para verificar se namespace existe
check_namespace() {
    print_step "Verificando namespace '${NAMESPACE}'..."
    if ! kubectl get namespace "${NAMESPACE}" &> /dev/null; then
        print_error "Namespace '${NAMESPACE}' não existe"
        exit 1
    fi
    print_success "Namespace '${NAMESPACE}' encontrado"
}

# Função para atualizar imagem de um deployment
update_deployment_image() {
    local deployment=$1
    local image="${DOCKER_HUB_USERNAME}/${deployment}:${TAG}"

    print_step "Atualizando imagem do deployment '${deployment}' para '${image}'..."

    if kubectl set image deployment/"${deployment}" \
        "${deployment}=${image}" \
        -n "${NAMESPACE}"; then
        print_success "Imagem do deployment '${deployment}' atualizada"
    else
        print_error "Falha ao atualizar imagem do deployment '${deployment}'"
        return 1
    fi
}

# Função para fazer rollout restart de um deployment
restart_deployment() {
    local deployment=$1

    print_step "Fazendo rollout restart do deployment '${deployment}'..."

    if kubectl rollout restart deployment/"${deployment}" -n "${NAMESPACE}"; then
        print_success "Rollout restart iniciado para '${deployment}'"
    else
        print_error "Falha ao fazer rollout restart do deployment '${deployment}'"
        return 1
    fi
}

# Função para aguardar rollout de um deployment
wait_rollout() {
    local deployment=$1

    print_step "Aguardando rollout do deployment '${deployment}'..."

    if kubectl rollout status deployment/"${deployment}" -n "${NAMESPACE}" --timeout=5m; then
        print_success "Rollout do deployment '${deployment}' concluído"
    else
        print_error "Timeout ou falha no rollout do deployment '${deployment}'"
        return 1
    fi
}

# Função para verificar pods de um deployment
check_pods() {
    local deployment=$1

    print_step "Verificando pods do deployment '${deployment}'..."
    kubectl get pods -n "${NAMESPACE}" -l app="${deployment}" -o wide
}

# Função para exibir logs recentes
show_recent_logs() {
    local deployment=$1

    print_step "Exibindo logs recentes do deployment '${deployment}'..."
    local pod=$(kubectl get pods -n "${NAMESPACE}" -l app="${deployment}" -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)

    if [ -n "${pod}" ]; then
        kubectl logs "${pod}" -n "${NAMESPACE}" --tail=20
    else
        print_warning "Nenhum pod encontrado para '${deployment}'"
    fi
}

# Função para fazer rollout de um serviço específico
rollout_service() {
    local service=$1

    echo -e "${BLUE}====================================="
    echo "Processando: ${service}"
    echo "=====================================${NC}"

    update_deployment_image "${service}" || return 1
    restart_deployment "${service}" || return 1
    wait_rollout "${service}" || return 1
    check_pods "${service}"
    echo ""
}

# Função para exibir menu
show_menu() {
    echo -e "${BLUE}"
    echo "=============================================="
    echo "  Redeploy Microserviços no EKS"
    echo "  Namespace: ${NAMESPACE}"
    echo "  Tag: ${TAG}"
    echo "=============================================="
    echo -e "${NC}"
    echo "Escolha uma opção:"
    echo "  1) Redeploy de TODOS os serviços"
    echo "  2) Redeploy de um serviço específico"
    echo "  3) Apenas restart (sem atualizar imagem)"
    echo "  4) Verificar status dos deployments"
    echo "  5) Sair"
    echo ""
}

# Função para redeploy de todos os serviços
redeploy_all() {
    print_step "Iniciando redeploy de todos os serviços..."

    success_count=0
    fail_count=0
    failed_services=()

    for service in "${DEPLOYMENTS[@]}"; do
        if rollout_service "${service}"; then
            ((success_count++))
        else
            ((fail_count++))
            failed_services+=("${service}")
        fi
    done

    # Resumo
    echo ""
    echo -e "${BLUE}=============================================="
    echo "  RESUMO"
    echo "==============================================\${NC}"
    print_success "Sucesso: ${success_count}/${#DEPLOYMENTS[@]}"

    if [ ${fail_count} -gt 0 ]; then
        print_error "Falhas: ${fail_count}/${#DEPLOYMENTS[@]}"
        print_warning "Serviços que falharam:"
        for service in "${failed_services[@]}"; do
            echo "  - ${service}"
        done
    else
        print_success "Todos os serviços foram atualizados com sucesso!"
    fi
}

# Função para selecionar serviço específico
select_service() {
    echo ""
    echo "Serviços disponíveis:"
    local i=1
    for service in "${DEPLOYMENTS[@]}"; do
        echo "  ${i}) ${service}"
        ((i++))
    done
    echo ""

    read -p "Digite o número do serviço: " choice

    if [[ "$choice" =~ ^[0-9]+$ ]] && [ "$choice" -ge 1 ] && [ "$choice" -le "${#DEPLOYMENTS[@]}" ]; then
        local selected_service="${DEPLOYMENTS[$((choice-1))]}"
        rollout_service "${selected_service}"
    else
        print_error "Opção inválida"
    fi
}

# Função para restart sem atualizar imagem
restart_all() {
    print_step "Fazendo restart de todos os deployments..."

    for service in "${DEPLOYMENTS[@]}"; do
        restart_deployment "${service}"
    done

    print_step "Aguardando conclusão dos rollouts..."
    for service in "${DEPLOYMENTS[@]}"; do
        wait_rollout "${service}"
    done

    print_success "Restart de todos os serviços concluído!"
}

# Função para verificar status
check_status() {
    print_step "Status dos deployments no namespace '${NAMESPACE}':"
    echo ""
    kubectl get deployments -n "${NAMESPACE}"
    echo ""
    print_step "Pods no namespace '${NAMESPACE}':"
    echo ""
    kubectl get pods -n "${NAMESPACE}" -o wide
}

# Função principal
main() {
    check_kubectl
    check_cluster_connection
    check_namespace

    # Se argumentos foram passados, executar diretamente
    if [ $# -gt 0 ]; then
        case "$1" in
            all)
                redeploy_all
                ;;
            restart)
                restart_all
                ;;
            status)
                check_status
                ;;
            *)
                print_error "Argumento inválido: $1"
                echo "Uso: $0 [all|restart|status]"
                exit 1
                ;;
        esac
        exit 0
    fi

    # Menu interativo
    while true; do
        show_menu
        read -p "Opção: " option

        case $option in
            1)
                redeploy_all
                ;;
            2)
                select_service
                ;;
            3)
                restart_all
                ;;
            4)
                check_status
                ;;
            5)
                print_step "Saindo..."
                exit 0
                ;;
            *)
                print_error "Opção inválida"
                ;;
        esac

        echo ""
        read -p "Pressione Enter para continuar..."
    done
}

# Executar script
main "$@"
