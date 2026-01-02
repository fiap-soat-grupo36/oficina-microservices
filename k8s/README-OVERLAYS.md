# Kustomize Overlays - Guia de Deploy Multi-Ambiente

Este diretório foi reestruturado para suportar deploy em **3 ambientes diferentes** usando Kustomize overlays.

## 📁 Estrutura

```
k8s/
├── base/                           # Manifestos base comuns a todos os ambientes
│   ├── kustomization.yaml
│   ├── postgres.yaml
│   ├── eureka-server.yaml
│   ├── *-service.yaml              # 7 microserviços
│   ├── configmap-shared.yaml
│   └── hpa.yaml
│
└── overlays/
    ├── local/                      # Ambiente LOCAL (Minikube)
    │   ├── kustomization.yaml
    │   ├── namespace.yaml          # Namespace: oficina
    │   ├── secret-*.yaml           # Secrets hardcoded para dev
    │   └── ingress.yaml            # Host: oficina.local
    │
    ├── dev/                        # Ambiente DEV (EKS via Terraform)
    │   ├── kustomization.yaml
    │   ├── namespace.yaml          # Namespace: oficina-mecanica-dev
    │   ├── secrets.yaml            # Secrets de dev (trocar valores!)
    │   └── ingress.yaml            # Host: dev.oficina-mecanica.com
    │
    └── prod/                       # Ambiente PROD (EKS via Terraform)
        ├── kustomization.yaml
        ├── namespace.yaml          # Namespace: oficina-mecanica-prod
        ├── secrets.yaml            # ATENÇÃO: Usar secrets manager!
        ├── ingress.yaml            # Host: oficina-mecanica.com
        └── patches/
            ├── replicas.yaml       # 2 réplicas base
            ├── resources.yaml      # Mais CPU/memória
            └── hpa.yaml            # HPA: min 2, max 5
```

## 🚀 Como Usar

### 1️⃣ LOCAL - Minikube

Para desenvolvimento local no Minikube:

```bash
# Iniciar Minikube
minikube start --cpus=4 --memory=8192

# Aplicar manifestos do overlay local
kubectl apply -k k8s/overlays/local

# Verificar pods
kubectl -n oficina get pods

# Acessar serviços (port-forward)
kubectl -n oficina port-forward svc/eureka-server 8761:8761

# Ou configurar ingress
minikube addons enable ingress
echo "$(minikube ip) oficina.local" | sudo tee -a /etc/hosts
# Acesse: http://oficina.local/eureka
```

**Características do overlay local:**
- ✅ Namespace: `oficina`
- ✅ Secrets hardcoded (seguros para dev local)
- ✅ Ingress: `oficina.local`
- ✅ Resources: 150m CPU / 256Mi RAM
- ✅ HPA: min 1, max 2

---

### 2️⃣ DEV - EKS via Terraform

Para ambiente de desenvolvimento no EKS (gerenciado pelo Terraform):

#### Opção A: Deploy via Terraform (Recomendado)

```bash
cd infra

# Selecionar workspace dev
terraform workspace select dev

# Aplicar (Terraform aplicará automaticamente o overlay dev)
terraform apply
```

O Terraform irá:
1. Criar namespace `oficina-mecanica-dev`
2. Aplicar metrics-server
3. Executar `kubectl apply -k ../k8s/overlays/dev`

#### Opção B: Deploy Manual (se Terraform não gerenciar k8s)

```bash
# Configurar kubectl para o cluster EKS
aws eks update-kubeconfig --name eks-fiap-oficina-mecanica --region us-east-2

# Aplicar overlay dev
kubectl apply -k k8s/overlays/dev

# Verificar
kubectl -n oficina-mecanica-dev get pods
```

**Características do overlay dev:**
- ✅ Namespace: `oficina-mecanica-dev`
- ⚠️ Secrets: Valores de exemplo (TROCAR ANTES DE USAR!)
- ✅ Ingress: `dev.oficina-mecanica.com`
- ✅ Resources: 150m CPU / 256Mi RAM
- ✅ HPA: min 1, max 2
- ✅ imagePullPolicy: Always

---

### 3️⃣ PROD - EKS via Terraform

Para ambiente de produção no EKS:

#### Deploy via Terraform

```bash
cd infra

# Selecionar workspace prod
terraform workspace select prod

# Aplicar
terraform apply
```

#### Deploy Manual (GitOps)

```bash
# Configurar kubectl
aws eks update-kubeconfig --name eks-fiap-oficina-mecanica --region us-east-2

# Aplicar overlay prod
kubectl apply -k k8s/overlays/prod

# Verificar
kubectl -n oficina-mecanica-prod get pods
```

**Características do overlay prod:**
- ✅ Namespace: `oficina-mecanica-prod`
- 🔒 Secrets: **USAR AWS SECRETS MANAGER!**
- ✅ Ingress: `oficina-mecanica.com` (configurar TLS)
- ⚡ Resources: 250m CPU / 512Mi RAM (requests), 1000m / 2Gi (limits)
- 📈 Replicas: 2 base
- 📊 HPA: min 2, max 5
- ✅ imagePullPolicy: Always

---

## 🔐 Gestão de Secrets

### Local (Minikube)
✅ Secrets hardcoded nos arquivos (OK para dev local)

### Dev (EKS)
⚠️ Atualmente usa secrets hardcoded em `overlays/dev/secrets.yaml`
- **TROCAR os valores** antes de usar
- Considere usar AWS Secrets Manager para melhorar segurança

### Prod (EKS)
🔒 **CRÍTICO**: Não use secrets hardcoded em produção!

**Opções recomendadas:**

#### Opção 1: AWS Secrets Manager + External Secrets Operator

```bash
# 1. Instalar External Secrets Operator
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets -n external-secrets --create-namespace

# 2. Criar secrets no AWS Secrets Manager
aws secretsmanager create-secret --name oficina-prod-db-password --secret-string "sua-senha-segura"
aws secretsmanager create-secret --name oficina-prod-jwt-secret --secret-string "seu-jwt-secret-256-bits"

# 3. Criar ExternalSecret no k8s
kubectl apply -f overlays/prod/external-secrets.yaml
```

#### Opção 2: Sealed Secrets

```bash
# 1. Instalar Sealed Secrets Controller
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.24.0/controller.yaml

# 2. Criptografar secrets
kubeseal -f overlays/prod/secrets.yaml -w overlays/prod/sealed-secrets.yaml

# 3. Commitar apenas a versão criptografada
git add overlays/prod/sealed-secrets.yaml
```

#### Opção 3: Terraform Secrets (tfvars não-commitado)

```bash
# Passar secrets via variável do Terraform
terraform apply -var db_password="senha-segura" -var jwt_secret="jwt-secret"
```

---

## 🔄 Atualizando Imagens

### Desenvolvimento (Local/Dev)
```bash
# Build das imagens
make build TAG=v1.0.0

# Load no Minikube (local)
eval "$(minikube docker-env)"
make build TAG=v1.0.0

# Push para Docker Hub (dev/prod)
make push REGISTRY=grecomilani TAG=v1.0.0

# Atualizar deployments
kubectl -n oficina set image deployment/auth-service auth-service=grecomilani/oficina-auth-service:v1.0.0
# Ou simplesmente reaplique o overlay
kubectl apply -k k8s/overlays/dev
```

### Produção
```bash
# 1. Build e push com versionamento
make push REGISTRY=grecomilani TAG=v1.0.0

# 2. Atualizar tag nos manifestos base (se necessário)
# Ou usar Kustomize images transformer

# 3. Aplicar via Terraform
cd infra
terraform workspace select prod
terraform apply

# Ou via kubectl
kubectl apply -k k8s/overlays/prod
```

---

## 🎯 Diferenças Entre Ambientes

| Característica | Local | Dev | Prod |
|---------------|-------|-----|------|
| Namespace | oficina | oficina-mecanica-dev | oficina-mecanica-prod |
| Gerenciado por | kubectl | Terraform | Terraform |
| Secrets | Hardcoded OK | Hardcoded (trocar!) | AWS Secrets Manager |
| Ingress Host | oficina.local | dev.oficina-mecanica.com | oficina-mecanica.com |
| TLS/SSL | Não | Opcional | Obrigatório |
| Replicas Base | 1 | 1 | 2 |
| HPA Min/Max | 1/2 | 1/2 | 2/5 |
| CPU Request | 150m | 150m | 250m |
| Memory Request | 256Mi | 256Mi | 512Mi |
| CPU Limit | 500m | 500m | 1000m |
| Memory Limit | 1Gi | 1Gi | 2Gi |
| imagePullPolicy | IfNotPresent | Always | Always |

---

## 🧪 Testando Overlays Localmente

Antes de aplicar, você pode visualizar os manifestos gerados:

```bash
# Ver manifestos do overlay local
kubectl kustomize k8s/overlays/local

# Ver manifestos do overlay dev
kubectl kustomize k8s/overlays/dev

# Ver manifestos do overlay prod
kubectl kustomize k8s/overlays/prod

# Salvar em arquivo para review
kubectl kustomize k8s/overlays/prod > /tmp/prod-manifests.yaml
```

---

## 🐛 Troubleshooting

### Erro: namespace already exists
```bash
# O namespace já foi criado pelo Terraform, remova do kustomization
# Ou delete o namespace antes
kubectl delete namespace oficina-mecanica-dev
```

### Erro: secrets não encontrados
```bash
# Verifique se aplicou os secrets
kubectl -n oficina-mecanica-dev get secrets

# Reaplique o overlay
kubectl apply -k k8s/overlays/dev
```

### Erro: ingress não funciona
```bash
# Verifique se o ingress controller está instalado
kubectl get pods -n ingress-nginx

# No Minikube, habilite o addon
minikube addons enable ingress

# No EKS, verifique se o NLB foi criado
kubectl get svc -n ingress-nginx
```

### Pods ficam em CrashLoopBackOff
```bash
# Ver logs
kubectl -n oficina logs -f deployment/auth-service

# Causas comuns:
# 1. Secrets errados (DB_PASSWORD, JWT_SECRET)
# 2. Banco não disponível
# 3. Eureka não acessível
```

---

## 📚 Mais Informações

- [Kustomize Documentation](https://kustomize.io/)
- [Kubernetes Namespaces](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/)
- [External Secrets Operator](https://external-secrets.io/)
- [Sealed Secrets](https://github.com/bitnami-labs/sealed-secrets)
