-- Saldos iniciais de produtos
INSERT INTO produto_estoque (produto_catalogo_id, quantidade_total, quantidade_reservada, quantidade_disponivel,
                             preco_custo_medio, preco_medio_sugerido, estoque_minimo, updated_at)
VALUES (1, 50, 0, 50, 35.90, 46.67, 10, CURRENT_TIMESTAMP),
       (2, 30, 0, 30, 45.00, 58.50, 5, CURRENT_TIMESTAMP),
       (3, 40, 0, 40, 120.00, 156.00, 8, CURRENT_TIMESTAMP);

-- Movimentações iniciais
INSERT INTO movimentacao_estoque (produto_catalogo_id, tipo_movimentacao, quantidade, preco_unitario, data_movimentacao,
                                  observacao)
VALUES (1, 'ENTRADA', 50, 35.90, CURRENT_TIMESTAMP, 'Estoque inicial'),
       (2, 'ENTRADA', 30, 45.00, CURRENT_TIMESTAMP, 'Estoque inicial'),
       (3, 'ENTRADA', 40, 120.00, CURRENT_TIMESTAMP, 'Estoque inicial');
