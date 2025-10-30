package br.com.fiap.oficina.catalog.dto.response;

import br.com.fiap.oficina.shared.enums.CategoriaServico;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicoResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private CategoriaServico categoria;
    private BigDecimal precoBase;
    private Long tempoEstimadoMinutos;
    private Boolean ativo;
}
