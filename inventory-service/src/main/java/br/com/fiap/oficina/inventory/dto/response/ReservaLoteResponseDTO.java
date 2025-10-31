package br.com.fiap.oficina.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaLoteResponseDTO {
    private Long ordemServicoId;
    private Boolean sucessoGeral;
    private List<ItemReservaResultDTO> resultados;
}
