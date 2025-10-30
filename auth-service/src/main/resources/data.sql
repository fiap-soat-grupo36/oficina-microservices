-- Usuários de teste (senhas: admin123, cliente123, mecanico123, atendente123, estoquista123)
INSERT INTO usuarios (username, nome, password, role, ativo) VALUES
('admin', 'Administrador do Sistema', '$2a$10$N9qo8uLOickgx2ZMRZoMye1UpxJbVo8M3ks0kLFhK1qVXJ8L2/3ei', 'ADMIN', true),
('cliente', 'Cliente Teste', '$2a$10$r6f8CqLXJ8xXx3XJ8xXx3efQUw0xYT0xYT0xYT0xYT0xYT0xYT0xY', 'CLIENTE', true),
('mecanico', 'Mecânico Teste', '$2a$10$m9qo8uLOickgx2ZMRZoMye1UpxJbVo8M3ks0kLFhK1qVXJ8L2/3ei', 'MECANICO', true),
('atendente', 'Atendente Teste', '$2a$10$a9qo8uLOickgx2ZMRZoMye1UpxJbVo8M3ks0kLFhK1qVXJ8L2/3ei', 'ATENDENTE', true),
('estoquista', 'Estoquista Teste', '$2a$10$e9qo8uLOickgx2ZMRZoMye1UpxJbVo8M3ks0kLFhK1qVXJ8L2/3ei', 'ESTOQUISTA', true);
