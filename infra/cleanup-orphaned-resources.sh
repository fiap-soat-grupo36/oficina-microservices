#!/bin/bash

echo "========================================="
echo "Limpando recursos órfãos do Terraform State"
echo "========================================="
echo ""

# Verificar workspace
WORKSPACE=$(terraform workspace show)
echo "Workspace atual: $WORKSPACE"
echo ""

# 1. Remover kubectl_manifest.eureka_nlb_service
echo "1. Removendo kubectl_manifest.eureka_nlb_service..."
if terraform state list | grep -q "kubectl_manifest.eureka_nlb_service"; then
    terraform state rm 'kubectl_manifest.eureka_nlb_service'
    echo "   ✅ Removido com sucesso"
else
    echo "   ℹ️  Recurso não encontrado no state (já foi removido)"
fi
echo ""

# 2. Remover kubernetes_namespace.datadog (se existir)
echo "2. Removendo kubernetes_namespace.datadog..."
if terraform state list | grep -q "kubernetes_namespace.datadog"; then
    terraform state rm 'kubernetes_namespace.datadog'
    echo "   ✅ Removido com sucesso"
else
    echo "   ℹ️  Recurso não encontrado no state"
fi
echo ""

# 3. Remover kubernetes_secret.datadog (se existir)
echo "3. Removendo kubernetes_secret.datadog..."
if terraform state list | grep -q "kubernetes_secret.datadog"; then
    terraform state rm 'kubernetes_secret.datadog'
    echo "   ✅ Removido com sucesso"
else
    echo "   ℹ️  Recurso não encontrado no state"
fi
echo ""

# 4. Remover todos kubectl_manifest.dd_agent_manifest (se existirem)
echo "4. Removendo kubectl_manifest.dd_agent_manifest[*]..."
DD_MANIFESTS=$(terraform state list | grep "kubectl_manifest.dd_agent_manifest" || true)
if [ -n "$DD_MANIFESTS" ]; then
    while IFS= read -r resource; do
        echo "   Removendo: $resource"
        terraform state rm "$resource"
    done <<< "$DD_MANIFESTS"
    echo "   ✅ Todos os manifests dd_agent removidos"
else
    echo "   ℹ️  Nenhum manifest dd_agent encontrado no state"
fi
echo ""

# 5. Remover data.kubectl_path_documents.dd_agent (se existir)
echo "5. Removendo data.kubectl_path_documents.dd_agent..."
if terraform state list | grep -q "data.kubectl_path_documents.dd_agent"; then
    terraform state rm 'data.kubectl_path_documents.dd_agent'
    echo "   ✅ Removido com sucesso"
else
    echo "   ℹ️  Recurso não encontrado no state"
fi
echo ""

echo "========================================="
echo "✅ Limpeza concluída!"
echo "========================================="
echo ""
echo "Próximos passos:"
echo "1. Execute: terraform plan"
echo "2. Verifique se não há mais erros de recursos órfãos"
echo "3. Execute: terraform apply"
