package br.com.fiap.oficina.inventory.service;

import br.com.fiap.oficina.inventory.entity.ReservaEstoque;

import java.util.List;

public interface ReservaEstoqueService {
    
    ReservaEstoque reservar(Long produtoCatalogoId, Long ordemServicoId, Integer quantidade);
    
    void cancelarPorOrdemServico(Long ordemServicoId);
    
    List<ReservaEstoque> listarReservasPorOrdemServico(Long ordemServicoId);
}
