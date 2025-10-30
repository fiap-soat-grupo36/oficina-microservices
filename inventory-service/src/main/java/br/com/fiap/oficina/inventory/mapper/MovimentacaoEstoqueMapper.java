package br.com.fiap.oficina.inventory.mapper;

import br.com.fiap.oficina.inventory.dto.request.MovimentacaoEstoqueRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.MovimentacaoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.entity.MovimentacaoEstoque;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MovimentacaoEstoqueMapper {

    @Mapping(source = "tipoMovimentacao", target = "tipoMovimentacao")
    MovimentacaoEstoque toEntity(MovimentacaoEstoqueRequestDTO dto);

    @Mapping(source = "produtoCatalogoId", target = "produtoId")
    @Mapping(source = "tipoMovimentacao", target = "tipo")
    MovimentacaoEstoqueResponseDTO toResponseDTO(MovimentacaoEstoque entity);
}
