package br.com.fiap.oficina.inventory.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProdutoEntradaEstoqueResponseDTO {
    private Long id;
    private Long produtoCatalogoId;
    private String nomeProduto;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal valorTotal;
    private String numeroNotaFiscal;
    private String fornecedor;
    private String observacoes;
    private LocalDateTime dataEntrada;
    private String usuarioRegistro;
}
