#!/bin/bash

# Criar pasta api-docs se não existir
mkdir -p ./api-docs

services=(
  "auth-service:8082"
  "customer-service:8081"
  "catalog-service:8083"
  "inventory-service:8084"
  "budget-service:8085"
  "work-order-service:8086"
)

echo "📁 Pasta ./api-docs criada/verificada!"
echo ""

for service in "${services[@]}"; do
  name="${service%%:*}"
  port="${service##*:}"
  echo "⬇️  Baixando $name..."

  # Fazer o download e verificar se deu erro
  if curl -s "http://localhost:${port}/v3/api-docs" > "./api-docs/${name}-api-docs.json"; then
    # Verificar se o arquivo tem conteúdo válido (não está vazio e não é erro)
    if [ -s "./api-docs/${name}-api-docs.json" ] && grep -q "openapi" "./api-docs/${name}-api-docs.json"; then
      echo "✅ ${name}-api-docs.json salvo!"
    else
      echo "⚠️  ${name} retornou erro ou arquivo vazio!"
      cat "./api-docs/${name}-api-docs.json"
    fi
  else
    echo "❌ Falha ao conectar com ${name}"
  fi
  echo ""
done

echo "🎉 Processo finalizado!"
echo "📊 Arquivos salvos em: ./api-docs/"