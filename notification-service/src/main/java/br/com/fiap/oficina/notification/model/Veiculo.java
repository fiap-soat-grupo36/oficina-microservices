package br.com.fiap.oficina.notification.model;

import lombok.Data;

@Data
public class Veiculo {
    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
}
