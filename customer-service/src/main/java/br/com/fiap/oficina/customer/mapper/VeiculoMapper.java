package br.com.fiap.oficina.customer.mapper;

import br.com.fiap.oficina.customer.dto.request.VeiculoRequesDTO;
import br.com.fiap.oficina.customer.dto.response.VeiculoResponseDTO;
import br.com.fiap.oficina.customer.entity.Veiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VeiculoMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    VeiculoResponseDTO toDTO(Veiculo veiculo);

    Veiculo toEntity(VeiculoRequesDTO veiculoResponseDTO);

    List<VeiculoResponseDTO> toDTOList(List<Veiculo> veiculos);
}