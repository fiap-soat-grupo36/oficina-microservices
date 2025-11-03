package br.com.fiap.oficina.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemReservaDTO {

    @NotNull(message = "produtoCatalogoId é obrigatório")
    private Long produtoCatalogoId;

    @NotNull(message = "quantidade é obrigatória")
    @Min(value = 1, message = "quantidade deve ser no mínimo 1")
    private Integer quantidade;
}
