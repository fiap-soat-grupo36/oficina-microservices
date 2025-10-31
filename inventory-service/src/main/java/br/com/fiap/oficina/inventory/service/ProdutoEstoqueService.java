package br.com.fiap.oficina.inventory.service;

import br.com.fiap.oficina.inventory.dto.response.ProdutoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.entity.ProdutoEstoque;

import java.util.List;

public interface ProdutoEstoqueService {
    
    ProdutoEstoque obterOuCriarSaldo(Long produtoCatalogoId);
    
    void atualizarSaldoAposMovimentacao(Long produtoCatalogoId);
    
    void recalcularPrecoMedio(Long produtoCatalogoId);
    
    ProdutoEstoqueResponseDTO getSaldoConsolidado(Long produtoCatalogoId);
    
    // New methods for complete controller
    List<ProdutoEstoqueResponseDTO> listarTodos();
    
    ProdutoEstoqueResponseDTO buscarPorId(Long id);
    
    ProdutoEstoqueResponseDTO buscarPorProdutoCatalogo(Long produtoCatalogoId);
    
    List<ProdutoEstoqueResponseDTO> buscarPorTermo(String termo);
    
    List<ProdutoEstoqueResponseDTO> listarBaixoEstoque();
    
    ProdutoEstoqueResponseDTO atualizarEstoqueMinimo(Long id, Integer estoqueMinimo);
}
