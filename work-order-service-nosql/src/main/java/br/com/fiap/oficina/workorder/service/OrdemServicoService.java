package br.com.fiap.oficina.workorder.service;

import br.com.fiap.oficina.shared.enums.StatusOrdemServico;
import br.com.fiap.oficina.workorder.dto.request.ItemOrdemServicoDTO;
import br.com.fiap.oficina.workorder.dto.request.OsRequestDTO;
import br.com.fiap.oficina.workorder.dto.response.OrdemServicoResponseDTO;
import br.com.fiap.oficina.workorder.dto.response.OrdemServicoResumoDTO;
import br.com.fiap.oficina.workorder.dto.response.OsItemDTO;
import br.com.fiap.oficina.workorder.entity.OrdemServico;

import java.util.List;

public interface OrdemServicoService {

    OrdemServicoResponseDTO criar(OsRequestDTO request);

    List<OrdemServicoResponseDTO> listarTodos(List<StatusOrdemServico> status);

    OrdemServicoResponseDTO buscarPorId(String id);

    List<OrdemServicoResumoDTO> buscarPorCliente(Long clienteId);

    List<OrdemServicoResumoDTO> buscarPorVeiculo(Long veiculoId);

    List<OrdemServicoResponseDTO> buscarPorMecanico(Long mecanicoId);

    List<OrdemServicoResponseDTO> buscarAtualizadas();

    OrdemServicoResponseDTO atualizar(String id, OsRequestDTO request);

    void atualizarStatus(String ordemServicoId, StatusOrdemServico status);

    OrdemServicoResponseDTO atribuirMecanico(String id, Long mecanicoId);

    OrdemServicoResponseDTO diagnosticar(String id, String observacoes);

    OrdemServicoResponseDTO executar(String id, String observacoes);

    OrdemServicoResponseDTO finalizar(String id, String observacoes);

    OrdemServicoResponseDTO entregar(String id, String observacoes);

    List<OsItemDTO> adicionarServicos(String id, List<Long> servicosIds);

    OrdemServicoResponseDTO removerServicos(String id, List<Long> servicosIds);

    List<OsItemDTO> adicionarProdutos(String id, List<ItemOrdemServicoDTO> produtos);

    OrdemServicoResponseDTO removerProdutos(String id, List<Long> produtosIds);

    void deletar(String id);

    OrdemServico getOrdemServico(String id);

    void adicionarOrcamento(String ordemServicoId, Long orcamentoId, StatusOrdemServico status);
}
