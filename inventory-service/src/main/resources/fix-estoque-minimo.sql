-- Script para corrigir dados existentes no banco de dados
-- Execute este script caso tenha dados com estoque_minimo NULL

-- Atualizar registros existentes com estoque_minimo NULL para 0
UPDATE produto_estoque
SET estoque_minimo = 0
WHERE estoque_minimo IS NULL;

-- Verificar se há registros com estoque_minimo NULL
SELECT COUNT(*) as registros_com_null
FROM produto_estoque
WHERE estoque_minimo IS NULL;
