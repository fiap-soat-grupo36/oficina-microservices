package br.com.fiap.oficina.workorder.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OsRequestDTO {

    @NotNull(message = "Cliente ID é obrigatório")
    private Long clienteId;

    @NotNull(message = "Veículo ID é obrigatório")
    private Long veiculoId;

    private String observacoes;

    private List<Long> servicosIds;

    private List<ItemOrdemServicoDTO> produtos;
}
