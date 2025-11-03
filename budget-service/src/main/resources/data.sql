-- Orçamentos vinculados a OS
INSERT INTO orcamento (ordem_servico_id, status_orcamento, valor_total, data_criacao, ativo)
VALUES (1, 'CRIADO', 685.90, CURRENT_TIMESTAMP, true),
       (2, 'APROVADO', 780.50, CURRENT_TIMESTAMP, true),
       (3, 'REPROVADO', 1200.00, CURRENT_TIMESTAMP, true);

-- Itens de orçamento (produtos com reserva)
INSERT INTO item_orcamento (orcamento_id, descricao, quantidade, valor_unitario, valor_total, reserva_estoque_id)
VALUES (1, 'Filtro de Óleo', 1, 35.90, 35.90, 1),
       (1, 'Óleo Motor 5W30', 4, 45.00, 180.00, 2);

-- Itens de orçamento (serviços)
INSERT INTO item_orcamento (orcamento_id, descricao, quantidade, valor_unitario, valor_total)
VALUES (1, 'Troca de Óleo', 1, 120.00, 120.00),
       (1, 'Revisão Completa', 1, 350.00, 350.00),
       (2, 'Alinhamento e Balanceamento', 1, 180.50, 180.50),
       (2, 'Troca de Pneus', 4, 150.00, 600.00),
       (3, 'Revisão de 10.000km', 1, 450.00, 450.00),
       (3, 'Troca de Filtros', 1, 250.00, 250.00),
       (3, 'Inspeção Completa', 1, 500.00, 500.00);
