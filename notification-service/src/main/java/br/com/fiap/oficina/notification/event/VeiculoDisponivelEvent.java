package br.com.fiap.oficina.notification.event;

import br.com.fiap.oficina.notification.model.Cliente;
import br.com.fiap.oficina.notification.model.Veiculo;

public record VeiculoDisponivelEvent(Cliente clienteResumo, Veiculo veiculoResumo, String token) {}
