package br.com.fiap.oficina.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaLoteRequestDTO {

    @NotNull(message = "ordemServicoId é obrigatório")
    private Long ordemServicoId;

    @NotEmpty(message = "A lista de itens não pode estar vazia")
    @Valid
    private List<ItemReservaDTO> itens;

    private Boolean allOrNothing = false;
}
