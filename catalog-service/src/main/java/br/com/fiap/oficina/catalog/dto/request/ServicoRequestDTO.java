package br.com.fiap.oficina.catalog.dto.request;

import br.com.fiap.oficina.shared.enums.CategoriaServico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServicoRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "Categoria é obrigatória")
    private CategoriaServico categoria;

    @NotNull(message = "Preço base é obrigatório")
    @Positive(message = "Preço base deve ser positivo")
    private BigDecimal precoBase;

    @Positive(message = "Tempo estimado deve ser positivo")
    private Long tempoEstimadoMinutos;
}
