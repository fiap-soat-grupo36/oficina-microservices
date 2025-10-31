package br.com.fiap.oficina.notification.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemOrcamento {
    private Long id;
    private String descricao;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
}
