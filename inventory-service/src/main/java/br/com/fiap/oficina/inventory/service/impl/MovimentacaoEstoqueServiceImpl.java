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
        List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository
                .findWithFilters(produtoCatalogoId, tipo, dataInicio, dataFim);

        return movimentacoes.stream()
                .map(movimentacaoEstoqueMapper::toResponseDTO)
                .collect(Collectors.toList());
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
