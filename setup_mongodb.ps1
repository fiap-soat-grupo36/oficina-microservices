# ============================================================================
# SETUP MONGODB PARA WORK ORDER SERVICE - VERSÃO WINDOWS
# ============================================================================

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "🚀 SETUP MONGODB - WORK ORDER SERVICE" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================================
# PASSO 1: VERIFICAR DOCKER
# ============================================================================

Write-Host "📦 Verificando Docker..." -ForegroundColor Yellow

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker não está instalado!" -ForegroundColor Red
    Write-Host "   Instale o Docker Desktop: https://docs.docker.com/desktop/install/windows-install/" -ForegroundColor Red
    exit 1
}

try {
    docker info | Out-Null
} catch {
    Write-Host "❌ Docker Desktop não está rodando!" -ForegroundColor Red
    Write-Host "   Inicie o Docker Desktop e tente novamente" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Docker OK" -ForegroundColor Green
Write-Host ""

# ============================================================================
# PASSO 2: LIMPAR CONTAINERS ANTIGOS
# ============================================================================

Write-Host "🧹 Limpando containers antigos..." -ForegroundColor Yellow

docker stop oficina-mongodb 2>$null | Out-Null
docker rm oficina-mongodb 2>$null | Out-Null

Write-Host "✅ Cleanup OK" -ForegroundColor Green
Write-Host ""

# ============================================================================
# PASSO 3: CRIAR MONGODB
# ============================================================================

Write-Host "🐳 Criando container MongoDB..." -ForegroundColor Yellow

docker run -d `
  --name oficina-mongodb `
  -p 27017:27017 `
  -e MONGO_INITDB_ROOT_USERNAME=admin `
  -e MONGO_INITDB_ROOT_PASSWORD=admin123 `
  -v oficina-mongodb-data:/data/db `
  mongo:6

Write-Host "⏳ Aguardando MongoDB iniciar (15 segundos)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Verificar se está rodando
$running = docker ps | Select-String "oficina-mongodb"
if (-not $running) {
    Write-Host "❌ Erro ao iniciar MongoDB!" -ForegroundColor Red
    docker logs oficina-mongodb
    exit 1
}

Write-Host "✅ MongoDB rodando!" -ForegroundColor Green
Write-Host ""

# ============================================================================
# PASSO 4: CRIAR BANCO E USUÁRIO
# ============================================================================

Write-Host "🔐 Criando banco de dados e usuário..." -ForegroundColor Yellow

$createUserScript = @"
use workorder_db

db.createUser({
  user: 'workorder_user',
  pwd: 'workorder_pass',
  roles: [
    { role: 'readWrite', db: 'workorder_db' }
  ]
})

print('✅ Banco workorder_db criado!')
print('✅ Usuário workorder_user criado!')
"@

$createUserScript | docker exec -i oficina-mongodb mongosh --username admin --password admin123 --authenticationDatabase admin

Write-Host ""

# ============================================================================
# PASSO 5: CRIAR COLLECTION E ÍNDICES
# ============================================================================

Write-Host "📊 Criando collection e índices..." -ForegroundColor Yellow

$createCollectionScript = @"
use workorder_db

db.createCollection('ordens_servico')

db.ordens_servico.createIndex({ 'status': 1 })
db.ordens_servico.createIndex({ 'cliente_id': 1 })
db.ordens_servico.createIndex({ 'veiculo_id': 1 })
db.ordens_servico.createIndex({ 'mecanico_id': 1 })
db.ordens_servico.createIndex({ 'data_criacao': -1 })
db.ordens_servico.createIndex({ 'cliente_cache.nome': 1 })
db.ordens_servico.createIndex({ 'veiculo_cache.placa': 1 })

print('✅ Collection ordens_servico criada!')
print('✅ 7 índices criados!')
"@

$createCollectionScript | docker exec -i oficina-mongodb mongosh --username workorder_user --password workorder_pass --authenticationDatabase workorder_db

Write-Host ""

# ============================================================================
# PASSO 6: POPULAR COM DADOS DE TESTE
# ============================================================================

Write-Host "📝 Populando dados de teste..." -ForegroundColor Yellow

$insertDataScript = @"
use workorder_db

db.ordens_servico.deleteMany({})

db.ordens_servico.insertMany([
  {
    status: 'RECEBIDA',
    data_criacao: new Date('2025-02-10T08:30:00Z'),
    observacoes: 'Troca de óleo e revisão completa',
    veiculo_id: NumberLong(1),
    cliente_id: NumberLong(1),
    mecanico_id: null,
    orcamento_id: null,
    cliente_cache: {
      id: NumberLong(1),
      nome: 'João Silva',
      email: 'joao.silva@email.com',
      telefone: '(11) 98765-4321'
    },
    veiculo_cache: {
      id: NumberLong(1),
      placa: 'ABC1234',
      marca: 'Honda',
      modelo: 'Civic',
      ano: 2020
    },
    mecanico_cache: null,
    servicos_ids: [NumberLong(1), NumberLong(2)],
    itens: [
      { produtoCatalogoId: NumberLong(1), quantidade: 1, precoUnitario: NumberDecimal('35.90') },
      { produtoCatalogoId: NumberLong(2), quantidade: 4, precoUnitario: NumberDecimal('45.00') }
    ],
    historico_status: [
      {
        statusAnterior: null,
        statusNovo: 'RECEBIDA',
        dataHora: new Date('2025-02-10T08:30:00Z'),
        usuario: 'Atendente',
        observacao: 'OS criada no sistema'
      }
    ]
  },
  {
    status: 'EM_DIAGNOSTICO',
    data_criacao: new Date('2025-02-09T10:00:00Z'),
    observacoes: 'Barulho no motor, verificar pastilhas de freio',
    veiculo_id: NumberLong(2),
    cliente_id: NumberLong(2),
    mecanico_id: NumberLong(3),
    orcamento_id: null,
    cliente_cache: {
      id: NumberLong(2),
      nome: 'Maria Santos',
      email: 'maria.santos@email.com',
      telefone: '(11) 97654-3210'
    },
    veiculo_cache: {
      id: NumberLong(2),
      placa: 'DEF5678',
      marca: 'Toyota',
      modelo: 'Corolla',
      ano: 2019
    },
    mecanico_cache: {
      id: NumberLong(3),
      nome: 'Carlos Mecânico'
    },
    servicos_ids: [NumberLong(3), NumberLong(4)],
    itens: [
      { produtoCatalogoId: NumberLong(3), quantidade: 2, precoUnitario: NumberDecimal('120.00') }
    ],
    historico_status: [
      { statusAnterior: null, statusNovo: 'RECEBIDA', dataHora: new Date('2025-02-09T10:00:00Z'), usuario: 'Atendente', observacao: 'OS criada' },
      { statusAnterior: 'RECEBIDA', statusNovo: 'EM_DIAGNOSTICO', dataHora: new Date('2025-02-09T11:30:00Z'), usuario: 'Carlos Mecânico', observacao: 'Iniciando diagnóstico' }
    ]
  },
  {
    status: 'AGUARDANDO_APROVACAO',
    data_criacao: new Date('2025-02-08T14:00:00Z'),
    observacoes: 'Troca de pastilhas de freio e discos',
    veiculo_id: NumberLong(3),
    cliente_id: NumberLong(3),
    mecanico_id: NumberLong(3),
    orcamento_id: NumberLong(1),
    cliente_cache: {
      id: NumberLong(3),
      nome: 'Pedro Oliveira',
      email: 'pedro.oliveira@email.com',
      telefone: '(11) 96543-2109'
    },
    veiculo_cache: {
      id: NumberLong(3),
      placa: 'GHI9012',
      marca: 'Ford',
      modelo: 'Focus',
      ano: 2018
    },
    mecanico_cache: { id: NumberLong(3), nome: 'Carlos Mecânico' },
    servicos_ids: [NumberLong(3)],
    itens: [
      { produtoCatalogoId: NumberLong(4), quantidade: 4, precoUnitario: NumberDecimal('85.00') },
      { produtoCatalogoId: NumberLong(5), quantidade: 2, precoUnitario: NumberDecimal('150.00') }
    ],
    historico_status: [
      { statusAnterior: null, statusNovo: 'RECEBIDA', dataHora: new Date('2025-02-08T14:00:00Z'), usuario: 'Atendente', observacao: 'OS criada' },
      { statusAnterior: 'RECEBIDA', statusNovo: 'EM_DIAGNOSTICO', dataHora: new Date('2025-02-08T15:00:00Z'), usuario: 'Carlos Mecânico', observacao: 'Diagnóstico realizado' },
      { statusAnterior: 'EM_DIAGNOSTICO', statusNovo: 'AGUARDANDO_APROVACAO', dataHora: new Date('2025-02-08T16:30:00Z'), usuario: 'Sistema', observacao: 'Orçamento gerado' }
    ]
  },
  {
    status: 'EM_EXECUCAO',
    data_criacao: new Date('2025-02-07T09:00:00Z'),
    data_inicio_execucao: new Date('2025-02-08T08:00:00Z'),
    observacoes: 'Alinhamento e balanceamento',
    veiculo_id: NumberLong(4),
    cliente_id: NumberLong(4),
    mecanico_id: NumberLong(4),
    orcamento_id: NumberLong(2),
    cliente_cache: {
      id: NumberLong(4),
      nome: 'Ana Costa',
      email: 'ana.costa@email.com',
      telefone: '(11) 95432-1098'
    },
    veiculo_cache: { id: NumberLong(4), placa: 'JKL3456', marca: 'Chevrolet', modelo: 'Onix', ano: 2021 },
    mecanico_cache: { id: NumberLong(4), nome: 'Roberto Mecânico' },
    servicos_ids: [NumberLong(5)],
    itens: [],
    historico_status: [
      { statusAnterior: null, statusNovo: 'RECEBIDA', dataHora: new Date('2025-02-07T09:00:00Z'), usuario: 'Atendente', observacao: 'OS criada' },
      { statusAnterior: 'RECEBIDA', statusNovo: 'EM_DIAGNOSTICO', dataHora: new Date('2025-02-07T10:00:00Z'), usuario: 'Roberto Mecânico', observacao: 'Verificando alinhamento' },
      { statusAnterior: 'EM_DIAGNOSTICO', statusNovo: 'AGUARDANDO_APROVACAO', dataHora: new Date('2025-02-07T11:00:00Z'), usuario: 'Sistema', observacao: 'Orçamento gerado' },
      { statusAnterior: 'AGUARDANDO_APROVACAO', statusNovo: 'EM_EXECUCAO', dataHora: new Date('2025-02-08T08:00:00Z'), usuario: 'Roberto Mecânico', observacao: 'Cliente aprovou' }
    ]
  },
  {
    status: 'FINALIZADA',
    data_criacao: new Date('2025-02-05T11:00:00Z'),
    data_inicio_execucao: new Date('2025-02-06T08:00:00Z'),
    data_termino_execucao: new Date('2025-02-06T16:00:00Z'),
    observacoes: 'Troca de correia dentada',
    veiculo_id: NumberLong(5),
    cliente_id: NumberLong(5),
    mecanico_id: NumberLong(3),
    orcamento_id: NumberLong(3),
    cliente_cache: { id: NumberLong(5), nome: 'Lucas Ferreira', email: 'lucas.ferreira@email.com', telefone: '(11) 94321-0987' },
    veiculo_cache: { id: NumberLong(5), placa: 'MNO7890', marca: 'Volkswagen', modelo: 'Polo', ano: 2022 },
    mecanico_cache: { id: NumberLong(3), nome: 'Carlos Mecânico' },
    servicos_ids: [NumberLong(2), NumberLong(6)],
    itens: [
      { produtoCatalogoId: NumberLong(6), quantidade: 1, precoUnitario: NumberDecimal('280.00') },
      { produtoCatalogoId: NumberLong(1), quantidade: 1, precoUnitario: NumberDecimal('35.90') }
    ],
    historico_status: [
      { statusAnterior: null, statusNovo: 'RECEBIDA', dataHora: new Date('2025-02-05T11:00:00Z'), usuario: 'Atendente', observacao: 'OS criada' },
      { statusAnterior: 'RECEBIDA', statusNovo: 'EM_DIAGNOSTICO', dataHora: new Date('2025-02-05T13:00:00Z'), usuario: 'Carlos Mecânico', observacao: 'Diagnóstico completo' },
      { statusAnterior: 'EM_DIAGNOSTICO', statusNovo: 'AGUARDANDO_APROVACAO', dataHora: new Date('2025-02-05T15:00:00Z'), usuario: 'Sistema', observacao: 'Orçamento gerado' },
      { statusAnterior: 'AGUARDANDO_APROVACAO', statusNovo: 'EM_EXECUCAO', dataHora: new Date('2025-02-06T08:00:00Z'), usuario: 'Carlos Mecânico', observacao: 'Orçamento aprovado' },
      { statusAnterior: 'EM_EXECUCAO', statusNovo: 'FINALIZADA', dataHora: new Date('2025-02-06T16:00:00Z'), usuario: 'Carlos Mecânico', observacao: 'Serviço concluído' }
    ]
  }
])

print('✅ 5 ordens de serviço inseridas!')
"@

$insertDataScript | docker exec -i oficina-mongodb mongosh --username workorder_user --password workorder_pass --authenticationDatabase workorder_db

Write-Host ""

# ============================================================================
# VERIFICAR
# ============================================================================

Write-Host "🔍 Verificando dados inseridos..." -ForegroundColor Yellow

$count = docker exec oficina-mongodb mongosh --quiet --username workorder_user --password workorder_pass --authenticationDatabase workorder_db --eval "use workorder_db; db.ordens_servico.countDocuments()"

Write-Host "✅ Total de documentos: $count" -ForegroundColor Green
Write-Host ""

# ============================================================================
# INFORMAÇÕES FINAIS
# ============================================================================

Write-Host "==================================================" -ForegroundColor Green
Write-Host "✅ SETUP CONCLUÍDO COM SUCESSO!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
Write-Host ""
Write-Host "📝 INFORMAÇÕES DE CONEXÃO:" -ForegroundColor Cyan
Write-Host ""
Write-Host "  URI para development local:" -ForegroundColor Yellow
Write-Host "  mongodb://workorder_user:workorder_pass@localhost:27017/workorder_db" -ForegroundColor White
Write-Host ""
Write-Host "  URI para Docker Compose:" -ForegroundColor Yellow
Write-Host "  mongodb://workorder_user:workorder_pass@mongodb:27017/workorder_db" -ForegroundColor White
Write-Host ""
Write-Host "==================================================" -ForegroundColor Green
Write-Host ""
Write-Host "🎯 PRÓXIMOS PASSOS:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Passar URI acima para o desenvolvedor" -ForegroundColor White
Write-Host "2. Ele vai atualizar application.yml" -ForegroundColor White
Write-Host "3. Testar a aplicação Work Order Service" -ForegroundColor White
Write-Host ""
Write-Host "📊 COMANDOS ÚTEIS:" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Ver logs:" -ForegroundColor Yellow
Write-Host "  docker logs oficina-mongodb" -ForegroundColor White
Write-Host ""
Write-Host "  Parar MongoDB:" -ForegroundColor Yellow
Write-Host "  docker stop oficina-mongodb" -ForegroundColor White
Write-Host ""
Write-Host "  Iniciar MongoDB:" -ForegroundColor Yellow
Write-Host "  docker start oficina-mongodb" -ForegroundColor White
Write-Host ""
Write-Host "==================================================" -ForegroundColor Green
Write-Host "🚀 MongoDB pronto para uso!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
