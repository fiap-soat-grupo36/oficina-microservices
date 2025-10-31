package br.com.fiap.oficina.inventory.service;

import br.com.fiap.oficina.inventory.dto.request.MovimentacaoEstoqueRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.MovimentacaoEstoqueResponseDTO;
import br.com.fiap.oficina.shared.enums.TipoMovimentacao;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimentacaoEstoqueService {
    
    MovimentacaoEstoqueResponseDTO registrarEntrada(MovimentacaoEstoqueRequestDTO dto);
    
    MovimentacaoEstoqueResponseDTO registrarSaida(MovimentacaoEstoqueRequestDTO dto);
    
    List<MovimentacaoEstoqueResponseDTO> listarMovimentacoes(
            Long produtoCatalogoId,
            TipoMovimentacao tipo,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    );
    
    MovimentacaoEstoqueResponseDTO buscarPorId(Long id);
}
