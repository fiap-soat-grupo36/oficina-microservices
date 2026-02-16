package br.com.fiap.oficina.workorder.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemOrdemServicoDTO {

    private Long produtoCatalogoId;

    private Integer quantidade;

    private BigDecimal precoUnitario;
}
