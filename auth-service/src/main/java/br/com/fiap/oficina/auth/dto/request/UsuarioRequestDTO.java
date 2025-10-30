package br.com.fiap.oficina.auth.dto.request;

import br.com.fiap.oficina.shared.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioRequestDTO {

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @NotBlank(message = "Password é obrigatório")
    private String password;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "Role é obrigatório")
    private Role role;
}
