package br.com.fiap.oficina.inventory.dto.response;

import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEstoqueResponseDTO {
    private Long id;
    private Long produtoId;
    private TipoMovimentacao tipo;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private LocalDateTime dataMovimentacao;
    private String observacao;
}
