#!/bin/bash

echo "🚀 Iniciando testes E2E da Oficina Microservices..."
echo ""

# Verifica dependências
if ! command -v jq &> /dev/null; then
    echo "❌ jq não está instalado. Instale com: sudo apt-get install jq"
    exit 1
fi

if ! command -v curl &> /dev/null; then
    echo "❌ curl não está instalado."
    exit 1
fi

# Torna scripts executáveis
chmod +x test-e2e.sh
chmod +x cleanup.sh

# Pergunta se deseja limpar dados anteriores
if [ -f "results/ids.env" ]; then
    echo "⚠️  Dados de teste anteriores encontrados."
    read -p "Deseja limpar antes de executar? (s/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        ./cleanup.sh
    fi
fi

# Executa testes
./test-e2e.sh

# Pergunta se deseja limpar após teste
echo ""
read -p "Deseja limpar os dados de teste agora? (s/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
    ./cleanup.sh
fi
