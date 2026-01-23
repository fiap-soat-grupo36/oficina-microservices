package br.com.fiap.oficina.inventory.service.impl;

import br.com.fiap.oficina.inventory.dto.request.MovimentacaoEstoqueRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.MovimentacaoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.entity.MovimentacaoEstoque;
import br.com.fiap.oficina.inventory.mapper.MovimentacaoEstoqueMapper;
import br.com.fiap.oficina.inventory.repository.MovimentacaoEstoqueRepository;
import br.com.fiap.oficina.inventory.service.MovimentacaoEstoqueService;
import br.com.fiap.oficina.inventory.service.ProdutoEstoqueService;
import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimentacaoEstoqueServiceImpl implements MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final ProdutoEstoqueService produtoEstoqueService;
    private final MovimentacaoEstoqueMapper movimentacaoEstoqueMapper;

    @Override
    @Transactional
    public MovimentacaoEstoqueResponseDTO registrarEntrada(MovimentacaoEstoqueRequestDTO dto) {
        dto.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        return registrarMovimentacao(dto);
    }

    @Override
    @Transactional
    public MovimentacaoEstoqueResponseDTO registrarSaida(MovimentacaoEstoqueRequestDTO dto) {
        dto.setTipoMovimentacao(TipoMovimentacao.SAIDA);
        return registrarMovimentacao(dto);
    }

    private MovimentacaoEstoqueResponseDTO registrarMovimentacao(MovimentacaoEstoqueRequestDTO dto) {
        // Criar movimentação
        MovimentacaoEstoque movimentacao = movimentacaoEstoqueMapper.toEntity(dto);
        movimentacao = movimentacaoEstoqueRepository.save(movimentacao);

        // Atualizar saldo do produto
        produtoEstoqueService.atualizarSaldoAposMovimentacao(dto.getProdutoCatalogoId());

        return movimentacaoEstoqueMapper.toResponseDTO(movimentacao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueResponseDTO> listarMovimentacoes(
            Long produtoCatalogoId,
            TipoMovimentacao tipo,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        List<MovimentacaoEstoque> movimentacoes;

        // Lógica condicional baseada nos parâmetros fornecidos
        boolean temProduto = produtoCatalogoId != null;
        boolean temTipo = tipo != null;
        boolean temDatas = dataInicio != null && dataFim != null;

        if (temProduto && temTipo && temDatas) {
            // Todos os filtros
            movimentacoes = movimentacaoEstoqueRepository
                    .findByProdutoCatalogoIdAndTipoMovimentacaoAndDataMovimentacaoBetween(
                            produtoCatalogoId, tipo, dataInicio, dataFim);
        } else if (temProduto && temTipo) {
            // Produto + Tipo
            movimentacoes = movimentacaoEstoqueRepository
                    .findByProdutoCatalogoIdAndTipoMovimentacao(produtoCatalogoId, tipo);
        } else if (temProduto && temDatas) {
            // Produto + Datas
            movimentacoes = movimentacaoEstoqueRepository
                    .findByProdutoCatalogoIdAndDataMovimentacaoBetween(produtoCatalogoId, dataInicio, dataFim);
        } else if (temTipo && temDatas) {
            // Tipo + Datas
            movimentacoes = movimentacaoEstoqueRepository
                    .findByTipoMovimentacaoAndDataMovimentacaoBetween(tipo, dataInicio, dataFim);
        } else if (temProduto) {
            // Só Produto
            movimentacoes = movimentacaoEstoqueRepository
                    .findByProdutoCatalogoId(produtoCatalogoId);
        } else if (temTipo) {
            // Só Tipo
            movimentacoes = movimentacaoEstoqueRepository
                    .findByTipoMovimentacao(tipo);
        } else if (temDatas) {
            // Só Datas
            movimentacoes = movimentacaoEstoqueRepository
                    .findByDataMovimentacaoBetween(dataInicio, dataFim);
        } else {
            // Sem filtros - todos os registros
            movimentacoes = movimentacaoEstoqueRepository.findAll();
        }

        return movimentacoes.stream()
                .map(movimentacaoEstoqueMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MovimentacaoEstoqueResponseDTO buscarPorId(Long id) {
        MovimentacaoEstoque movimentacao = movimentacaoEstoqueRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Movimentação não encontrada"));

        return movimentacaoEstoqueMapper.toResponseDTO(movimentacao);
    }
}
