package br.com.fiap.oficina.inventory.service;

import br.com.fiap.oficina.inventory.dto.request.ProdutoEntradaEstoqueRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.ProdutoEntradaEstoqueResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ProdutoEntradaEstoqueService {
    ProdutoEntradaEstoqueResponseDTO registrarEntrada(ProdutoEntradaEstoqueRequestDTO request);

    List<ProdutoEntradaEstoqueResponseDTO> listarEntradas(
            Long produtoCatalogoId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    );

    ProdutoEntradaEstoqueResponseDTO buscarPorId(Long id);
}
