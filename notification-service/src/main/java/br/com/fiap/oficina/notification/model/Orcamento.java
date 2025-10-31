package br.com.fiap.oficina.notification.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Orcamento {
    private Long id;
    private List<ItemOrcamento> itensOrcamento;
    private BigDecimal valorTotal;
    private LocalDateTime dataCriacao;
    private String token;
}
