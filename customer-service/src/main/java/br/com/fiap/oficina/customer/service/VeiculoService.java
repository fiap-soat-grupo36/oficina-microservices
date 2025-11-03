package br.com.fiap.oficina.customer.service;

import br.com.fiap.oficina.customer.dto.request.VeiculoRequesDTO;
import br.com.fiap.oficina.customer.dto.response.VeiculoResponseDTO;
import br.com.fiap.oficina.customer.entity.Veiculo;

import java.util.List;

public interface VeiculoService {

    VeiculoResponseDTO salvar(VeiculoRequesDTO dto);

    List<VeiculoResponseDTO> listarTodos();

    VeiculoResponseDTO buscarPorId(Long id);

    VeiculoResponseDTO buscarPorPlaca(String placa);

    List<VeiculoResponseDTO> buscarPorCliente(Long clienteId);

    VeiculoResponseDTO atualizar(Long id, VeiculoRequesDTO dto);

    VeiculoResponseDTO transferirPropriedade(Long veiculoId, Long novoClienteId);

    Veiculo getVeiculo(Long id);

    void deletar(Long id);
}