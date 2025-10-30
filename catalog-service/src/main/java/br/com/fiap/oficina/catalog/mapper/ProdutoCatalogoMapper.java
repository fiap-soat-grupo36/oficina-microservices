package br.com.fiap.oficina.catalog.mapper;

import br.com.fiap.oficina.catalog.dto.request.ProdutoCatalogoRequestDTO;
import br.com.fiap.oficina.catalog.dto.response.ProdutoCatalogoResponseDTO;
import br.com.fiap.oficina.catalog.entity.ProdutoCatalogo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProdutoCatalogoMapper {

    ProdutoCatalogoResponseDTO toDTO(ProdutoCatalogo produto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    ProdutoCatalogo toEntity(ProdutoCatalogoRequestDTO produtoDTO);

    List<ProdutoCatalogoResponseDTO> toDTO(List<ProdutoCatalogo> produtos);
}
