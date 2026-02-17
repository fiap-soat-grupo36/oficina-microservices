package br.com.fiap.oficina.workorder.dto.response;

import lombok.Data;

@Data
public class VeiculoResumoDTO {

    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
}
