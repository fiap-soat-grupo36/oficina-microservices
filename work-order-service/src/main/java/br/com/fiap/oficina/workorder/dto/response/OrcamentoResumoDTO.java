package br.com.fiap.oficina.workorder.dto.response;

import br.com.fiap.oficina.shared.enums.StatusOrcamento;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrcamentoResumoDTO {

    private Long id;
    private StatusOrcamento status;
    private BigDecimal valorTotal;
    private LocalDateTime dataCriacao;
}
