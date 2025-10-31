package br.com.fiap.oficina.workorder.dto.response;

import lombok.Data;

@Data
public class ClienteResumoDTO {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
}
