# Fix: NullPointerException no endpoint /api/estoque

## Problema Identificado

Erro ao chamar `/api/estoque`:
```json
{
  "timestamp": "2026-01-22T20:37:48.569297",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Ocorreu um erro inesperado: Cannot invoke \"java.lang.Integer.intValue()\" because the return value of \"br.com.fiap.oficina.inventory.entity.ProdutoEstoque.getEstoqueMinimo()\" is null"
}
```

## Causa Raiz

1. **Dados legados no banco**: O arquivo `data.sql` inseria registros sem a coluna `estoque_minimo`, deixando-a como NULL
2. **Mapper sem null-check**: O `ProdutoEstoqueMapper` fazia comparação direta sem verificar NULL:
   ```java
   entity.getQuantidadeDisponivel() < entity.getEstoqueMinimo()
   ```
3. **NullPointerException**: Ao tentar fazer unboxing de `null` para `int` primitivo

## Soluções Implementadas

### 1. Correção no Mapper (Proteção contra NULL)
```java
// ANTES:
@Mapping(target = "baixoEstoque", expression = "java(entity.getQuantidadeDisponivel() < entity.getEstoqueMinimo())")

// DEPOIS:
@Mapping(target = "baixoEstoque", expression = "java(entity.getQuantidadeDisponivel() != null && entity.getEstoqueMinimo() != null && entity.getQuantidadeDisponivel() < entity.getEstoqueMinimo())")
```

### 2. Correção na Entidade (Prevenção de NULL)
```java
// ANTES:
@Column(name = "estoque_minimo")
private Integer estoqueMinimo = 0;

// DEPOIS:
@Column(name = "estoque_minimo", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
private Integer estoqueMinimo = 0;
```

### 3. Correção no data.sql (Dados Iniciais)
```sql
-- ANTES:
INSERT INTO produto_estoque (produto_catalogo_id, quantidade_total, ..., updated_at)
VALUES (1, 50, ..., CURRENT_TIMESTAMP);

-- DEPOIS:
INSERT INTO produto_estoque (produto_catalogo_id, quantidade_total, ..., estoque_minimo, updated_at)
VALUES (1, 50, ..., 10, CURRENT_TIMESTAMP);
```

### 4. Script de Correção para Banco Existente

Se você já tem dados no banco, execute o script `fix-estoque-minimo.sql`:
```sql
UPDATE produto_estoque 
SET estoque_minimo = 0 
WHERE estoque_minimo IS NULL;
```

## Como Aplicar a Correção

### Opção 1: Banco de Dados Vazio (Desenvolvimento)
```bash
# Recrie o banco ou deixe o Hibernate recriar as tabelas
# O data.sql já está corrigido

# Recompile e execute
mvn clean package -DskipTests
docker-compose up -d inventory-service
```

### Opção 2: Banco de Dados com Dados Existentes (Produção)
```bash
# 1. Execute o script de correção
psql -U postgres -d oficina-db -f src/main/resources/fix-estoque-minimo.sql

# 2. Recompile e faça deploy
mvn clean package -DskipTests
docker-compose up -d inventory-service
```

## Verificação

Após aplicar a correção, teste o endpoint:
```bash
curl -X GET http://localhost:8084/api/estoque \
  -H "Authorization: Bearer <seu-token>"
```

Deve retornar sucesso (200) com a lista de produtos em estoque.

## Prevenção Futura

As seguintes proteções foram implementadas:

1. ✅ **Null-check no mapper** - Evita NullPointerException
2. ✅ **nullable=false na entidade** - Previne NULL no banco
3. ✅ **DEFAULT no banco** - Garante valor padrão
4. ✅ **Valor default na classe Java** - Proteção em tempo de execução
5. ✅ **data.sql atualizado** - Novos ambientes já vêm corretos

## Arquivos Modificados

- `ProdutoEstoqueMapper.java` - Null-check adicionado
- `ProdutoEstoque.java` - nullable=false e columnDefinition adicionados
- `data.sql` - Coluna estoque_minimo adicionada aos INSERTs
- `fix-estoque-minimo.sql` - Script de correção criado (novo arquivo)
