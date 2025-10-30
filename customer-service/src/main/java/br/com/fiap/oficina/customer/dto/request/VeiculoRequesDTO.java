package br.com.fiap.oficina.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VeiculoRequesDTO {

    @NotBlank(message = "A placa é obrigatória")
    @Pattern(regexp = "^[A-Z]{3}[0-9][A-Z][0-9]{2}$|^[A-Z]{3}[0-9]{4}$",
            message = "Placa inválida. Use o padrão Mercosul (ex: ABC1D23) ou o padrão antigo (ex: ABC1234).")
    private String placa;
    private String marca;

    @NotBlank(message = "O modelo é obrigatório")
    private String modelo;

    @NotNull(message = "O ano é obrigatório")
    private Integer ano;
    private String cor;
    private String observacoes;

    @NotNull(message = "O cliente é obrigatório")
    private Long clienteId;
}