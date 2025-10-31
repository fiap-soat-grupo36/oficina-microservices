package br.com.fiap.oficina.workorder.dto.response;

import lombok.Data;

@Data
public class OsItemDTO {

    private Long id;
    private String nome;
    private Boolean ativo;
    private String descricao;
}
