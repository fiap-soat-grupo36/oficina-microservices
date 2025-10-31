package br.com.fiap.oficina.inventory.service.impl;

import br.com.fiap.oficina.inventory.dto.response.ProdutoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.entity.MovimentacaoEstoque;
import br.com.fiap.oficina.inventory.entity.ProdutoEstoque;
import br.com.fiap.oficina.inventory.mapper.ProdutoEstoqueMapper;
import br.com.fiap.oficina.inventory.repository.MovimentacaoEstoqueRepository;
import br.com.fiap.oficina.inventory.repository.ProdutoEstoqueRepository;
import br.com.fiap.oficina.inventory.repository.ReservaEstoqueRepository;
import br.com.fiap.oficina.inventory.service.ProdutoEstoqueService;
import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProdutoEstoqueServiceImpl implements ProdutoEstoqueService {

    private final ProdutoEstoqueRepository produtoEstoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final ReservaEstoqueRepository reservaEstoqueRepository;
    private final ProdutoEstoqueMapper produtoEstoqueMapper;

    @Override
    @Transactional
    public ProdutoEstoque obterOuCriarSaldo(Long produtoCatalogoId) {
        return produtoEstoqueRepository.findByProdutoCatalogoId(produtoCatalogoId)
                .orElseGet(() -> {
                    ProdutoEstoque novoProduto = new ProdutoEstoque();
                    novoProduto.setProdutoCatalogoId(produtoCatalogoId);
                    novoProduto.setQuantidadeTotal(0);
                    novoProduto.setQuantidadeReservada(0);
                    novoProduto.setQuantidadeDisponivel(0);
                    novoProduto.setPrecoCustoMedio(BigDecimal.ZERO);
                    novoProduto.setPrecoMedioSugerido(BigDecimal.ZERO);
                    return produtoEstoqueRepository.save(novoProduto);
                });
    }

    @Override
    @Transactional
    public void atualizarSaldoAposMovimentacao(Long produtoCatalogoId) {
        ProdutoEstoque saldo = obterOuCriarSaldo(produtoCatalogoId);
        
        List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository
                .findByProdutoCatalogoId(produtoCatalogoId);
        
        int quantidadeTotal = 0;
        for (MovimentacaoEstoque mov : movimentacoes) {
            if (mov.getTipoMovimentacao() == TipoMovimentacao.ENTRADA) {
                quantidadeTotal += mov.getQuantidade();
            } else if (mov.getTipoMovimentacao() == TipoMovimentacao.SAIDA) {
                quantidadeTotal -= mov.getQuantidade();
            }
        }
        
        int quantidadeReservada = reservaEstoqueRepository
                .findByProdutoCatalogoIdAndAtivaTrue(produtoCatalogoId)
                .stream()
                .mapToInt(r -> r.getQuantidadeReservada())
                .sum();
        
        saldo.setQuantidadeTotal(quantidadeTotal);
        saldo.setQuantidadeReservada(quantidadeReservada);
        saldo.setQuantidadeDisponivel(quantidadeTotal - quantidadeReservada);
        
        produtoEstoqueRepository.save(saldo);
        recalcularPrecoMedio(produtoCatalogoId);
    }

    @Override
    @Transactional
    public void recalcularPrecoMedio(Long produtoCatalogoId) {
        ProdutoEstoque saldo = produtoEstoqueRepository
                .findByProdutoCatalogoId(produtoCatalogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado no estoque"));
        
        List<MovimentacaoEstoque> entradas = movimentacaoEstoqueRepository
                .findByProdutoCatalogoId(produtoCatalogoId)
                .stream()
                .filter(m -> m.getTipoMovimentacao() == TipoMovimentacao.ENTRADA)
                .toList();
        
        if (entradas.isEmpty() || saldo.getQuantidadeTotal() == 0) {
            saldo.setPrecoCustoMedio(BigDecimal.ZERO);
            saldo.setPrecoMedioSugerido(BigDecimal.ZERO);
        } else {
            BigDecimal custoTotal = entradas.stream()
                    .map(e -> e.getPrecoUnitario().multiply(BigDecimal.valueOf(e.getQuantidade())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal precoMedio = custoTotal.divide(
                    BigDecimal.valueOf(saldo.getQuantidadeTotal()),
                    2,
                    RoundingMode.HALF_UP
            );
            
            BigDecimal precoSugerido = precoMedio.multiply(BigDecimal.valueOf(1.30))
                    .setScale(2, RoundingMode.HALF_UP);
            
            saldo.setPrecoCustoMedio(precoMedio);
            saldo.setPrecoMedioSugerido(precoSugerido);
        }
        
        produtoEstoqueRepository.save(saldo);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoEstoqueResponseDTO getSaldoConsolidado(Long produtoCatalogoId) {
        ProdutoEstoque saldo = produtoEstoqueRepository
                .findByProdutoCatalogoId(produtoCatalogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado no estoque"));
        
        return produtoEstoqueMapper.toResponseDTO(saldo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoEstoqueResponseDTO> listarTodos() {
        return produtoEstoqueRepository.findAll()
                .stream()
                .map(produtoEstoqueMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoEstoqueResponseDTO buscarPorId(Long id) {
        ProdutoEstoque produto = produtoEstoqueRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado no estoque"));
        return produtoEstoqueMapper.toResponseDTO(produto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoEstoqueResponseDTO buscarPorProdutoCatalogo(Long produtoCatalogoId) {
        return getSaldoConsolidado(produtoCatalogoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoEstoqueResponseDTO> buscarPorTermo(String termo) {
        // Product name is not stored in inventory, requires catalog service integration
        // For now, return empty list - this endpoint requires catalog service client
        log.warn("buscarPorTermo called but product names not available in inventory service");
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoEstoqueResponseDTO> listarBaixoEstoque() {
        return produtoEstoqueRepository.findBaixoEstoque()
                .stream()
                .map(produtoEstoqueMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public ProdutoEstoqueResponseDTO atualizarEstoqueMinimo(Long id, Integer estoqueMinimo) {
        ProdutoEstoque produto = produtoEstoqueRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado no estoque"));
        
        produto.setEstoqueMinimo(estoqueMinimo);
        produto = produtoEstoqueRepository.save(produto);
        
        return produtoEstoqueMapper.toResponseDTO(produto);
    }
}
