package br.com.fiap.oficina.catalog.dto.response;

import br.com.fiap.oficina.shared.enums.CategoriaProduto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoCatalogoResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private CategoriaProduto categoria;
    private BigDecimal preco;
    private Boolean ativo;
}
