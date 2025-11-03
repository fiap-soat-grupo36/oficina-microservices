package br.com.fiap.oficina.inventory.dto.request;

import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEstoqueRequestDTO {

    @NotNull(message = "produtoCatalogoId é obrigatório")
    private Long produtoCatalogoId;

    @NotNull(message = "tipoMovimentacao é obrigatório")
    private TipoMovimentacao tipoMovimentacao;

    @NotNull(message = "quantidade é obrigatória")
    @Min(value = 1, message = "quantidade deve ser no mínimo 1")
    private Integer quantidade;

    @DecimalMin(value = "0.0", inclusive = true, message = "precoUnitario deve ser maior ou igual a zero")
    private BigDecimal precoUnitario;

    private String observacao;
}
