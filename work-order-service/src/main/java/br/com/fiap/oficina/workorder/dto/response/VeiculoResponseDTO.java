package br.com.fiap.oficina.workorder.dto.response;

import lombok.Data;

@Data
public class VeiculoResponseDTO {

    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private String observacoes;
    private Long clienteId;
    private Boolean ativo;
}
