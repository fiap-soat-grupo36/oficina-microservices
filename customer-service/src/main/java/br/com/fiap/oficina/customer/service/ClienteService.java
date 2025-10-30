package br.com.fiap.oficina.customer.service;

import br.com.fiap.oficina.customer.dto.request.ClienteRequestDTO;
import br.com.fiap.oficina.customer.dto.response.ClienteResponseDTO;
import br.com.fiap.oficina.customer.entity.Cliente;

import java.util.List;

public interface ClienteService {

    ClienteResponseDTO salvar(ClienteRequestDTO cliente);

    ClienteResponseDTO atualizar(Long id, ClienteRequestDTO cliente);

    ClienteResponseDTO buscarPorId(Long id);

    List<ClienteResponseDTO> buscarPorNome(String nome);

    List<ClienteResponseDTO> listarClientes();

    Cliente getCliente(Long id);

    void deletar(Long id);
}