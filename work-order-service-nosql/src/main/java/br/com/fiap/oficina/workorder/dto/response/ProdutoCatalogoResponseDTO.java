package br.com.fiap.oficina.workorder.dto.response;

import br.com.fiap.oficina.shared.enums.CategoriaProduto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProdutoCatalogoResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private CategoriaProduto categoria;
    private BigDecimal preco;
    private String unidadeMedida;
    private Boolean ativo;
}
