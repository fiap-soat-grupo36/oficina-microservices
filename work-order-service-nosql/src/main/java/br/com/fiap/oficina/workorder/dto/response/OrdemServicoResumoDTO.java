package br.com.fiap.oficina.workorder.dto.response;

import br.com.fiap.oficina.shared.enums.StatusOrdemServico;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrdemServicoResumoDTO {

    private String id;
    private StatusOrdemServico status;
    private LocalDateTime dataCriacao;
    private Long veiculoId;
    private Long clienteId;
    private Long mecanicoId;
    private Long orcamentoId;
}
