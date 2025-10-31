package br.com.fiap.oficina.inventory.service;

import br.com.fiap.oficina.inventory.dto.response.ProdutoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.entity.ProdutoEstoque;

public interface ProdutoEstoqueService {
    
    ProdutoEstoque obterOuCriarSaldo(Long produtoCatalogoId);
    
    void atualizarSaldoAposMovimentacao(Long produtoCatalogoId);
    
    void recalcularPrecoMedio(Long produtoCatalogoId);
    
    ProdutoEstoqueResponseDTO getSaldoConsolidado(Long produtoCatalogoId);
}
