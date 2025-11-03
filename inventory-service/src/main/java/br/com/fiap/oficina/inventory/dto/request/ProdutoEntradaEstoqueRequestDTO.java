package br.com.fiap.oficina.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProdutoEntradaEstoqueRequestDTO {
    @NotNull(message = "Produto é obrigatório")
    private Long produtoCatalogoId;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantidade;

    @NotNull(message = "Preço unitário é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal precoUnitario;

    private String numeroNotaFiscal;
    private String fornecedor;
    private String observacoes;
}
