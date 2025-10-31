package br.com.fiap.oficina.notification.model;

import lombok.Data;

@Data
public class Cliente {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
}
