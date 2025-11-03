-- Ordens de Serviço
INSERT INTO ordem_servico (status, data_criacao, observacoes, veiculo_id, cliente_id, mecanico_id)
VALUES ('RECEBIDA', CURRENT_TIMESTAMP, 'Troca de óleo e revisão', 1, 1, NULL),
       ('EM_EXECUCAO', CURRENT_TIMESTAMP, 'Manutenção preventiva', 2, 2, 3),
       ('FINALIZADA', CURRENT_TIMESTAMP, 'Troca de pastilhas de freio', 3, 3, 3);

-- Relacionamento OS-Serviços
INSERT INTO ordem_servico_servicos (ordem_servico_id, servico_id)
VALUES (1, 1),
       (1, 2),
       (2, 2),
       (3, 3);

-- Itens OS (produtos)
INSERT INTO item_ordem_servico (ordem_servico_id, produto_catalogo_id, quantidade, preco_unitario)
VALUES (1, 1, 1, 35.90),
       (1, 2, 4, 45.00),
       (2, 3, 2, 120.00);
