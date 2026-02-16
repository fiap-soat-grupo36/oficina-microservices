package br.com.fiap.oficina.workorder.dto.response;

import br.com.fiap.oficina.shared.vo.Endereco;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ClienteResponseDTO {

    private Long id;
    private String nome;
    private String cpf;
    private String cnpj;
    private String email;
    private String telefone;
    private Endereco endereco;
    private LocalDateTime dataCadastro;
    private LocalDate dataNascimento;
    private String observacao;
    private Boolean ativo;
}
