package br.com.fiap.oficina.catalog.service;

import br.com.fiap.oficina.catalog.dto.request.ServicoRequestDTO;
import br.com.fiap.oficina.catalog.dto.response.ServicoResponseDTO;
import br.com.fiap.oficina.catalog.entity.Servico;

import java.util.List;

public interface ServicoService {

    ServicoResponseDTO salvar(ServicoRequestDTO servico);

    ServicoResponseDTO atualizar(Long id, ServicoRequestDTO servico);

    ServicoResponseDTO buscarPorId(Long id);

    List<ServicoResponseDTO> listarTodos();

    List<ServicoResponseDTO> listarAtivos();

    List<ServicoResponseDTO> listarInativos();

    List<ServicoResponseDTO> buscarPorTermo(String termo);

    void deletar(Long id);

    Servico getServico(Long id);
}
