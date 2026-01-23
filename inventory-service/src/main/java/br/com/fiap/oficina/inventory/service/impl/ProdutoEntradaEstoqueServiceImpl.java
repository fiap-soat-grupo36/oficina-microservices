package br.com.fiap.oficina.inventory.service.impl;

import br.com.fiap.oficina.inventory.dto.request.ProdutoEntradaEstoqueRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.ProdutoEntradaEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.entity.MovimentacaoEstoque;
import br.com.fiap.oficina.inventory.repository.MovimentacaoEstoqueRepository;
import br.com.fiap.oficina.inventory.service.ProdutoEntradaEstoqueService;
import br.com.fiap.oficina.inventory.service.ProdutoEstoqueService;
import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProdutoEntradaEstoqueServiceImpl implements ProdutoEntradaEstoqueService {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final ProdutoEstoqueService produtoEstoqueService;

    @Override
    @Transactional
    public ProdutoEntradaEstoqueResponseDTO registrarEntrada(ProdutoEntradaEstoqueRequestDTO request) {
        log.info("Registrando entrada de estoque para produto ID: {}", request.getProdutoCatalogoId());

        // Ensure product stock exists
        produtoEstoqueService.obterOuCriarSaldo(request.getProdutoCatalogoId());

        // Create movement entry
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProdutoCatalogoId(request.getProdutoCatalogoId());
        movimentacao.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        movimentacao.setQuantidade(request.getQuantidade());
        movimentacao.setPrecoUnitario(request.getPrecoUnitario());
        movimentacao.setNumeroNotaFiscal(request.getNumeroNotaFiscal());
        movimentacao.setFornecedor(request.getFornecedor());
        movimentacao.setObservacao(request.getObservacoes());
        movimentacao.setUsuarioRegistro(getCurrentUsername());
        movimentacao.setDataMovimentacao(LocalDateTime.now());

        movimentacao = movimentacaoEstoqueRepository.save(movimentacao);

        // Update stock after entry
        produtoEstoqueService.atualizarSaldoAposMovimentacao(request.getProdutoCatalogoId());

        log.info("Entrada registrada com sucesso. ID: {}", movimentacao.getId());

        return mapToResponseDTO(movimentacao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoEntradaEstoqueResponseDTO> listarEntradas(
            Long produtoCatalogoId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim) {

        List<MovimentacaoEstoque> movimentacoes;

        // Lógica condicional para entradas
        boolean temProduto = produtoCatalogoId != null;
        boolean temDatas = dataInicio != null && dataFim != null;

        if (temProduto && temDatas) {
            // Produto + Datas + Tipo ENTRADA
            movimentacoes = movimentacaoEstoqueRepository
                    .findByProdutoCatalogoIdAndTipoMovimentacaoAndDataMovimentacaoBetween(
                            produtoCatalogoId, TipoMovimentacao.ENTRADA, dataInicio, dataFim);
        } else if (temProduto) {
            // Produto + Tipo ENTRADA
            movimentacoes = movimentacaoEstoqueRepository
                    .findByProdutoCatalogoIdAndTipoMovimentacao(produtoCatalogoId, TipoMovimentacao.ENTRADA);
        } else if (temDatas) {
            // Datas + Tipo ENTRADA
            movimentacoes = movimentacaoEstoqueRepository
                    .findByTipoMovimentacaoAndDataMovimentacaoBetween(TipoMovimentacao.ENTRADA, dataInicio, dataFim);
        } else {
            // Só Tipo ENTRADA
            movimentacoes = movimentacaoEstoqueRepository
                    .findByTipoMovimentacao(TipoMovimentacao.ENTRADA);
        }

        return movimentacoes.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoEntradaEstoqueResponseDTO buscarPorId(Long id) {
        MovimentacaoEstoque movimentacao = movimentacaoEstoqueRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Entrada não encontrada"));

        if (movimentacao.getTipoMovimentacao() != TipoMovimentacao.ENTRADA) {
            throw new RecursoNaoEncontradoException("Movimentação não é uma entrada");
        }

        return mapToResponseDTO(movimentacao);
    }

    private ProdutoEntradaEstoqueResponseDTO mapToResponseDTO(MovimentacaoEstoque movimentacao) {
        ProdutoEntradaEstoqueResponseDTO dto = new ProdutoEntradaEstoqueResponseDTO();
        dto.setId(movimentacao.getId());
        dto.setProdutoCatalogoId(movimentacao.getProdutoCatalogoId());
        // Known limitation: Product name cannot be set because integration with the catalog service is not yet implemented.
        // As a result, dto.setNomeProduto(null) will always set the product name to null.
        // To resolve this, implement a call to the catalog service to fetch the product name by produtoCatalogoId.
        dto.setNomeProduto(null);
        dto.setQuantidade(movimentacao.getQuantidade());
        dto.setPrecoUnitario(movimentacao.getPrecoUnitario());

        if (movimentacao.getPrecoUnitario() != null && movimentacao.getQuantidade() != null) {
            dto.setValorTotal(movimentacao.getPrecoUnitario()
                    .multiply(BigDecimal.valueOf(movimentacao.getQuantidade())));
        }

        dto.setNumeroNotaFiscal(movimentacao.getNumeroNotaFiscal());
        dto.setFornecedor(movimentacao.getFornecedor());
        dto.setObservacoes(movimentacao.getObservacao());
        dto.setDataEntrada(movimentacao.getDataMovimentacao());
        dto.setUsuarioRegistro(movimentacao.getUsuarioRegistro());

        return dto;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}
