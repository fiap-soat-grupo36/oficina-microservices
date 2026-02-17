package br.com.fiap.oficina.workorder.dto.response;

import br.com.fiap.oficina.shared.enums.CategoriaServico;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServicoResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private CategoriaServico categoria;
    private BigDecimal precoBase;
    private Long tempoEstimadoMinutos;
    private Boolean ativo;
}
