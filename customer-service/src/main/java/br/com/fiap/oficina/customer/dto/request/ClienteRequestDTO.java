package br.com.fiap.oficina.customer.dto.request;

import br.com.fiap.oficina.shared.vo.Endereco;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClienteRequestDTO {

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
    private LocalDate dataNascimento;
    private String observacao;
}