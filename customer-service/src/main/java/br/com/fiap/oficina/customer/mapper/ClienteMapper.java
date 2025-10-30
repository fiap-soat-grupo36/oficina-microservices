package br.com.fiap.oficina.customer.mapper;

import br.com.fiap.oficina.customer.dto.request.ClienteRequestDTO;
import br.com.fiap.oficina.customer.dto.response.ClienteResponseDTO;
import br.com.fiap.oficina.customer.entity.Cliente;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteResponseDTO toDTO(Cliente cliente);

    Cliente toEntity(ClienteRequestDTO clienteResponseDTO);

    List<ClienteResponseDTO> toDTO(List<Cliente> clientes);

    List<Cliente> toEntity(List<ClienteResponseDTO> clientesDTO);
}