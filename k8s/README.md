# Deploy dos Microserviços no Kubernetes

Este diretório contém os manifestos Kubernetes para executar todo o ecossistema da Oficina, incluindo o banco PostgreSQL, o Eureka Server e todos os microserviços.

**IMPORTANTE**: A estrutura foi migrada para **Kustomize overlays** para suportar múltiplos ambientes (Local/Dev/Prod).

## 🏗️ Nova Estrutura (Kustomize Overlays)

```
k8s/
├── base/                    # Manifestos base comuns
│   ├── kustomization.yaml
│   ├── postgres.yaml
│   ├── eureka-server.yaml
│   ├── *-service.yaml       # 7 microserviços
│   ├── configmap-shared.yaml
│   └── hpa.yaml
│
└── overlays/                # Configurações específicas por ambiente
    ├── local/               # Minikube (namespace: oficina)
    ├── dev/                 # EKS Dev (namespace: oficina-mecanica-dev)
    └── prod/                # EKS Prod (namespace: oficina-mecanica-prod)
```

## 🚀 Deploy Rápido

### Ambiente Local (Minikube)

```bash
# Iniciar Minikube
minikube start --cpus=4 --memory=8192
minikube addons enable ingress

# Aplicar manifestos
kubectl apply -k k8s/overlays/local

# Verificar pods
kubectl -n oficina get pods -w

# Acessar via port-forward
kubectl -n oficina port-forward svc/eureka-server 8761:8761
```

### Ambiente Dev (EKS via Terraform)

```bash
# Opção 1: Via Terraform (Recomendado)
cd infra
terraform workspace select dev
terraform apply

# Opção 2: Manual
aws eks update-kubeconfig --name eks-fiap-oficina-mecanica --region us-east-2
kubectl apply -k k8s/overlays/dev
kubectl -n oficina-mecanica-dev get pods
```

### Ambiente Prod (EKS via Terraform)

```bash
# Via Terraform
cd infra
terraform workspace select prod
terraform apply

# Manual
kubectl apply -k k8s/overlays/prod
kubectl -n oficina-mecanica-prod get pods
```

## 📋 Pré-requisitos

- Kubernetes 1.26+
- `kubectl` com suporte a Kustomize (1.14+)
- Imagens Docker publicadas no registro (grecomilani/oficina-*)
- Para EKS: Terraform configurado, cluster EKS provisionado

## 🔍 Visualizar Manifestos Sem Aplicar

```bash
# Ver o que será aplicado em cada ambiente
kubectl kustomize k8s/overlays/local
kubectl kustomize k8s/overlays/dev
kubectl kustomize k8s/overlays/prod
```

## 🌍 Diferenças Entre Ambientes

| Característica | Local | Dev | Prod |
|---------------|-------|-----|------|
| **Namespace** | oficina | oficina-mecanica-dev | oficina-mecanica-prod |
| **Gerenciado por** | kubectl manual | Terraform | Terraform |
| **Secrets** | Hardcoded (OK) | Hardcoded (trocar!) | AWS Secrets Manager |
| **Ingress Host** | oficina.local | dev.oficina-mecanica.com | oficina-mecanica.com |
| **Réplicas Base** | 1 | 1 | 2 |
| **HPA Min/Max** | 1/2 | 1/2 | 2/5 |
| **CPU Request** | 150m | 150m | 250m |
| **Memory Request** | 256Mi | 256Mi | 512Mi |
| **CPU Limit** | 500m | 500m | 1000m |
| **Memory Limit** | 1Gi | 1Gi | 2Gi |

## 🔐 Gestão de Secrets

### Local (Minikube)
✅ Secrets hardcoded nos arquivos são **seguros** para desenvolvimento local

### Dev (EKS)
⚠️ **TROCAR OS VALORES PADRÃO** em `overlays/dev/secrets.yaml` antes de usar!

### Prod (EKS)
🚨 **NUNCA use secrets hardcoded em produção!**

**Opções recomendadas:**
1. **AWS Secrets Manager + External Secrets Operator** (recomendado)
2. Sealed Secrets (GitOps-friendly)
3. Passar via Terraform variables

Ver documentação completa em: [README-OVERLAYS.md](./README-OVERLAYS.md)

## 📝 Estrutura dos Manifestos Base

- **namespace.yaml**: Namespace específico por ambiente
- **configmap-shared.yaml**: Variáveis de ambiente compartilhadas (URLs do Eureka, banco, etc.)
- **secret-*.yaml**: Credenciais (DB, JWT, email) - **específico por ambiente**
- **postgres.yaml**: StatefulSet + PVC do PostgreSQL (10Gi, storageClass: standard)
- **eureka-server.yaml**: Eureka Server (Service Registry)
- ***-service.yaml**: Deployments + Services dos 7 microserviços
- **hpa.yaml**: Horizontal Pod Autoscalers (todos os services)
- **ingress.yaml**: Nginx Ingress com path-based routing

## ⚙️ Configuração dos Microserviços

### Probes de Health
Todos os microserviços usam Spring Boot Actuator:
- **Liveness**: `GET /actuator/health` (initialDelay: 60s, period: 15s)
- **Readiness**: `GET /actuator/health` (initialDelay: 30s, period: 10s)

### Variáveis de Ambiente (ConfigMap)
```yaml
SPRING_PROFILES_ACTIVE: k8s
EUREKA_URL: http://eureka-server:8761/eureka/
DB_URL: jdbc:postgresql://postgres:5432/oficina-db
SERVER_PORT_*: 808X  # Porta de cada serviço
```

### Resources (Base - Dev/Local)
```yaml
requests:
  cpu: 150m
  memory: 256Mi
limits:
  cpu: 500m
  memory: 1Gi
```

### Resources (Produção - via patch)
```yaml
requests:
  cpu: 250m
  memory: 512Mi
limits:
  cpu: 1000m
  memory: 2Gi
```

## 🔧 Troubleshooting

### Pods não iniciam (CrashLoopBackOff)
```bash
# Ver logs
kubectl -n <namespace> logs -f deployment/auth-service

# Causas comuns:
# - Secrets errados (DB_PASSWORD, JWT_SECRET)
# - PostgreSQL não disponível
# - Eureka Server não acessível
```

### Namespace já existe (Terraform)
```bash
# O Terraform cria o namespace. Se aplicar manualmente, pode haver conflito.
# Solução: Remova namespace.yaml do overlay ou delete o namespace antes
kubectl delete namespace oficina-mecanica-dev
```

### Ingress não funciona
```bash
# Verificar se Ingress Controller está instalado
kubectl get pods -n ingress-nginx

# Minikube: habilitar addon
minikube addons enable ingress

# EKS: Verificar se NLB foi provisionado
kubectl get svc -n ingress-nginx
```

### StorageClass "standard" não encontrado
```bash
# Ver storageclasses disponíveis
kubectl get storageclass

# Editar postgres.yaml para usar a classe correta
# Ou criar um patch no overlay
```

## 📚 Documentação Completa

Para detalhes completos sobre:
- Como customizar cada ambiente
- Gestão avançada de secrets
- Deploy via Terraform
- Estratégias de GitOps
- Troubleshooting avançado

**Consulte**: [README-OVERLAYS.md](./README-OVERLAYS.md)

## 🆘 Suporte

Para problemas ou dúvidas:
1. Verifique os logs: `kubectl -n <namespace> logs -f <pod-name>`
2. Consulte o status: `kubectl -n <namespace> describe pod <pod-name>`
3. Revise a documentação: [README-OVERLAYS.md](./README-OVERLAYS.md)
4. Verifique o CLAUDE.md no diretório raiz para informações sobre a arquitetura
