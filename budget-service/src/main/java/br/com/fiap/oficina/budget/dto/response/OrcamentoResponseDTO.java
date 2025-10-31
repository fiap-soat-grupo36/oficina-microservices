package br.com.fiap.oficina.budget.dto.response;

import br.com.fiap.oficina.shared.enums.StatusOrcamento;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrcamentoResponseDTO {
    private Long id;
    private List<ItemOrcamentoDTO> itensOrcamento;
    private BigDecimal valorTotal;
    private StatusOrcamento statusOrcamento;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAprovacao;
    private LocalDateTime dataReprovacao;
}
