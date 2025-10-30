package br.com.fiap.oficina.budget.dto.response;

import br.com.fiap.oficina.shared.enums.StatusOrcamento;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrcamentoResumoDTO {
    private Long id;
    private BigDecimal valorTotal;
    private StatusOrcamento statusOrcamento;
}
