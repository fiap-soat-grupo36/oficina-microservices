package br.com.fiap.oficina.customer.dto.response;

import br.com.fiap.oficina.shared.vo.Endereco;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {

    private Long id;
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    private String cpf;
    private String cnpj;
    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;
    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;
    private Endereco endereco;
    private LocalDateTime dataCadastro;
    private LocalDate dataNascimento;
    private String observacao;
    private Boolean ativo;
}