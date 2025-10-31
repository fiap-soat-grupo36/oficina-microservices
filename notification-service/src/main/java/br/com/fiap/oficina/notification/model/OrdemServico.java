package br.com.fiap.oficina.notification.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrdemServico {
    private Long id;
    private Cliente cliente;
    private Veiculo veiculo;
    private LocalDateTime dataCriacao;
    private String observacoes;
}
