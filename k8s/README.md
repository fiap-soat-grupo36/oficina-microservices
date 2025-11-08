# Deploy dos Microserviços no Kubernetes

Este diretório contém os manifestos Kubernetes para executar todo o ecossistema da Oficina, incluindo o banco PostgreSQL, o Eureka Server e todos os microserviços.

## Pré-requisitos

- Kubernetes 1.26+
- `kubectl`
- `kustomize` (opcional; `kubectl` 1.14+ já possui `kustomize` integrado)
- Imagens dos microserviços publicadas em um registro acessível pelo cluster (atualize os nomes das imagens nos manifests conforme necessário)

## Como aplicar

```bash
# Ajuste as imagens antes, se necessário
kubectl apply -k k8s
```

> Caso seu cluster não possua uma *StorageClass* chamada `standard`, edite o arquivo [`postgres.yaml`](./postgres.yaml) e informe a classe desejada.

## Estrutura

- `namespace.yaml`: cria o namespace `oficina`.
- `configmap-shared.yaml`: variáveis de ambiente compartilhadas (URLs do Eureka, banco etc.).
- `secret-*.yaml`: credenciais de banco, JWT e email (substitua os valores conforme o ambiente).
- `postgres.yaml`: StatefulSet, serviço e volume persistente para o PostgreSQL.
- `*-service.yaml`: Deployments e Services dos microserviços.
- `kustomization.yaml`: arquivo de orquestração para aplicar tudo de uma vez.

## Observações

- As *probes* de liveness e readiness utilizam os endpoints `/actuator/health` de cada serviço Spring Boot.
- Os microserviços aguardam o Eureka via variável `EUREKA_URL` e o banco PostgreSQL via `DB_URL`.
- Ajuste as réplicas, *requests/limits* e *secrets* conforme as necessidades de produção.
