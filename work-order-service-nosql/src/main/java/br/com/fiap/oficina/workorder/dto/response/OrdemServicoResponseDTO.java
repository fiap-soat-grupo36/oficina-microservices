package br.com.fiap.oficina.workorder.dto.response;

import br.com.fiap.oficina.shared.enums.StatusOrdemServico;
import br.com.fiap.oficina.workorder.dto.request.ItemOrdemServicoDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdemServicoResponseDTO {

    private String id;
    private StatusOrdemServico status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataInicioExecucao;
    private LocalDateTime dataTerminoExecucao;
    private LocalDateTime dataEntrega;
    private String observacoes;
    private VeiculoResumoDTO veiculo;
    private ClienteResumoDTO cliente;
    private UsuarioResponseDTO mecanico;
    private List<ServicoResponseDTO> servicos;
    private List<ItemOrdemServicoDTO> itensOrdemServico;
    private OrcamentoResumoDTO orcamento;
}
