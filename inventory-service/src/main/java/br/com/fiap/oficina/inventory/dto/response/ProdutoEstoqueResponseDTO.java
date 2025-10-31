package br.com.fiap.oficina.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoEstoqueResponseDTO {
    private Long produtoId;
    private Integer quantidadeTotal;
    private Integer quantidadeReservada;
    private Integer quantidadeDisponivel;
    private BigDecimal precoCustoMedio;
    private BigDecimal precoMedioSugerido;
}
