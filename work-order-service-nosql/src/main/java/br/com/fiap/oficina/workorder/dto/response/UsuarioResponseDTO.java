package br.com.fiap.oficina.workorder.dto.response;

import lombok.Data;

@Data
public class UsuarioResponseDTO {

    private Long id;
    private String username;
    private String nome;
    private String role;
}
