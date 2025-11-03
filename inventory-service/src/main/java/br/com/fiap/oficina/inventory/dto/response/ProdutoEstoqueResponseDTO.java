package br.com.fiap.oficina.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("produtoCatalogoId")
    private Long produtoCatalogoId;

    // Legacy field mapping to same property
    @JsonProperty("produtoId")
    public Long getProdutoId() {
        return produtoCatalogoId;
    }

    private String nomeProduto;
    private String codigoProduto;
    private Integer quantidadeDisponivel;
    private Integer quantidadeReservada;
    private Integer quantidadeTotal;
    private Integer estoqueMinimo;
    private Boolean baixoEstoque;
    private LocalDateTime ultimaAtualizacao;
    private BigDecimal precoCustoMedio;
    private BigDecimal precoMedioSugerido;
}
