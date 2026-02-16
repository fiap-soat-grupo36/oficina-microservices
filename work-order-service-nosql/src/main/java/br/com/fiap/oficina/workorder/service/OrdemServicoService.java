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

    OrdemServicoResponseDTO buscarPorId(Long id);

    List<OrdemServicoResumoDTO> buscarPorCliente(Long clienteId);

    List<OrdemServicoResumoDTO> buscarPorVeiculo(Long veiculoId);

    List<OrdemServicoResponseDTO> buscarPorMecanico(Long mecanicoId);

    List<OrdemServicoResponseDTO> buscarAtualizadas();

    OrdemServicoResponseDTO atualizar(Long id, OsRequestDTO request);

    void atualizarStatus(Long orcamentoId, StatusOrdemServico status);

    OrdemServicoResponseDTO atribuirMecanico(Long id, Long mecanicoId);

    OrdemServicoResponseDTO diagnosticar(Long id, String observacoes);

    OrdemServicoResponseDTO executar(Long id, String observacoes);

    OrdemServicoResponseDTO finalizar(Long id, String observacoes);

    OrdemServicoResponseDTO entregar(Long id, String observacoes);

    List<OsItemDTO> adicionarServicos(Long id, List<Long> servicosIds);

    OrdemServicoResponseDTO removerServicos(Long id, List<Long> servicosIds);

    List<OsItemDTO> adicionarProdutos(Long id, List<ItemOrdemServicoDTO> produtos);

    OrdemServicoResponseDTO removerProdutos(Long id, List<Long> produtosIds);

    void deletar(Long id);

    OrdemServico getOrdemServico(Long id);

    void adicionarOrcamento(Long ordemServicoId, Long orcamentoId, StatusOrdemServico status);
}
