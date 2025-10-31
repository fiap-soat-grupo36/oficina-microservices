package br.com.fiap.oficina.inventory.mapper;

import br.com.fiap.oficina.inventory.dto.response.ProdutoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.entity.ProdutoEstoque;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProdutoEstoqueMapper {

    @Mapping(source = "produtoCatalogoId", target = "produtoCatalogoId")
    @Mapping(source = "updatedAt", target = "ultimaAtualizacao")
    @Mapping(target = "nomeProduto", ignore = true)
    @Mapping(target = "codigoProduto", ignore = true)
    @Mapping(target = "baixoEstoque", expression = "java(entity.getQuantidadeDisponivel() < entity.getEstoqueMinimo())")
    ProdutoEstoqueResponseDTO toResponseDTO(ProdutoEstoque entity);
}
