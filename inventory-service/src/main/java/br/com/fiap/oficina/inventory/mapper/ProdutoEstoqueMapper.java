package br.com.fiap.oficina.inventory.mapper;

import br.com.fiap.oficina.inventory.dto.response.ProdutoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.entity.ProdutoEstoque;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProdutoEstoqueMapper {

    @Mapping(source = "produtoCatalogoId", target = "produtoId")
    ProdutoEstoqueResponseDTO toResponseDTO(ProdutoEstoque entity);
}
