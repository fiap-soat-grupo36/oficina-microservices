package br.com.fiap.oficina.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoEstoqueResponseDTO {
    private Long id;
    private Long produtoCatalogoId;
    private String nomeProduto;
    private String codigoProduto;
    private Integer quantidadeDisponivel;
    private Integer quantidadeReservada;
    private Integer estoqueMinimo;
    private Boolean baixoEstoque;
    private LocalDateTime ultimaAtualizacao;
    
    // Legacy fields for backward compatibility
    private Long produtoId;
    private Integer quantidadeTotal;
    private BigDecimal precoCustoMedio;
    private BigDecimal precoMedioSugerido;
}
