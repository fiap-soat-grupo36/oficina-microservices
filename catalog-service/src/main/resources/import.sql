-- Produtos do Catálogo (8 produtos)
INSERT INTO produtos_catalogo (nome, descricao, categoria, preco, ativo)
VALUES ('Óleo de Motor 5W30', 'Óleo sintético para motor 5W30, 1 litro', 'INSUMO', 45.90, true);
INSERT INTO produtos_catalogo (nome, descricao, categoria, preco, ativo)
VALUES ('Filtro de Óleo', 'Filtro de óleo para motores a gasolina e flex', 'PECA', 28.50, true);
INSERT INTO produtos_catalogo (nome, descricao, categoria, preco, ativo)
VALUES ('Pastilha de Freio Dianteira', 'Jogo de pastilhas de freio dianteiro', 'PECA', 120.00, true);
INSERT INTO produtos_catalogo (nome, descricao, categoria, preco, ativo)
VALUES ('Disco de Freio', 'Disco de freio ventilado dianteiro', 'PECA', 180.00, true);
INSERT INTO produtos_catalogo (nome, descricao, categoria, preco, ativo)
VALUES ('Vela de Ignição', 'Vela de ignição platinada', 'PECA', 35.00, true);
INSERT INTO produtos_catalogo (nome, descricao, categoria, preco, ativo)
VALUES ('Bateria 60Ah', 'Bateria automotiva 60Ah selada', 'PECA', 350.00, true);
INSERT INTO produtos_catalogo (nome, descricao, categoria, preco, ativo)
VALUES ('Fluido de Freio DOT4', 'Fluido de freio DOT4, 500ml', 'INSUMO', 25.90, true);
INSERT INTO produtos_catalogo (nome, descricao, categoria, preco, ativo)
VALUES ('Lâmpada H7', 'Lâmpada halógena H7 para farol', 'PECA', 42.00, true);

-- Serviços (7 serviços)
INSERT INTO servicos (nome, descricao, categoria, preco_base, tempo_estimado, ativo)
VALUES ('Troca de Óleo', 'Troca de óleo do motor com filtro', 'MECANICO', 80.00, 45, true);
INSERT INTO servicos (nome, descricao, categoria, preco_base, tempo_estimado, ativo)
VALUES ('Alinhamento e Balanceamento', 'Alinhamento de direção e balanceamento de rodas', 'ALINHAMENTO', 120.00, 60,
        true);
INSERT INTO servicos (nome, descricao, categoria, preco_base, tempo_estimado, ativo)
VALUES ('Revisão de Freios', 'Inspeção completa do sistema de freios', 'FREIOS', 150.00, 90, true);
INSERT INTO servicos (nome, descricao, categoria, preco_base, tempo_estimado, ativo)
VALUES ('Troca de Pastilhas de Freio', 'Substituição de pastilhas de freio dianteiras', 'FREIOS', 200.00, 120, true);
INSERT INTO servicos (nome, descricao, categoria, preco_base, tempo_estimado, ativo)
VALUES ('Diagnóstico Eletrônico', 'Diagnóstico completo do sistema eletrônico do veículo', 'ELETRICO', 100.00, 60,
        true);
INSERT INTO servicos (nome, descricao, categoria, preco_base, tempo_estimado, ativo)
VALUES ('Revisão de Suspensão', 'Inspeção e ajuste do sistema de suspensão', 'SUSPENSAO', 180.00, 90, true);
INSERT INTO servicos (nome, descricao, categoria, preco_base, tempo_estimado, ativo)
VALUES ('Troca de Bateria', 'Substituição de bateria automotiva', 'ELETRICO', 50.00, 30, true);
