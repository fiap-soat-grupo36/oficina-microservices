package br.com.fiap.oficina.inventory.service;

import br.com.fiap.oficina.inventory.dto.request.MovimentacaoEstoqueRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.MovimentacaoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.entity.MovimentacaoEstoque;
import br.com.fiap.oficina.inventory.mapper.MovimentacaoEstoqueMapper;
import br.com.fiap.oficina.inventory.repository.MovimentacaoEstoqueRepository;
import br.com.fiap.oficina.inventory.service.impl.MovimentacaoEstoqueServiceImpl;
import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoEstoqueServiceImplTest {

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Mock
    private MovimentacaoEstoqueMapper movimentacaoEstoqueMapper;

    @Mock
    private ProdutoEstoqueService produtoEstoqueService;

    @InjectMocks
    private MovimentacaoEstoqueServiceImpl movimentacaoEstoqueService;

    @Test
    @DisplayName("Deve registrar entrada de estoque")
    void deveRegistrarEntradaDeEstoque() {
        // Arrange
        MovimentacaoEstoqueRequestDTO request = new MovimentacaoEstoqueRequestDTO();
        request.setProdutoCatalogoId(100L);
        request.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        request.setQuantidade(50);
        request.setPrecoUnitario(BigDecimal.TEN);

        MovimentacaoEstoque entity = new MovimentacaoEstoque();
        entity.setId(1L);
        entity.setProdutoCatalogoId(100L);
        entity.setTipoMovimentacao(TipoMovimentacao.ENTRADA);

        MovimentacaoEstoqueResponseDTO response = new MovimentacaoEstoqueResponseDTO();
        response.setId(1L);
        response.setTipoMovimentacao(TipoMovimentacao.ENTRADA);

        when(movimentacaoEstoqueMapper.toEntity(request)).thenReturn(entity);
        when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class))).thenReturn(entity);
        when(movimentacaoEstoqueMapper.toDTO(entity)).thenReturn(response);
        doNothing().when(produtoEstoqueService).atualizarSaldoAposMovimentacao(any());

        // Act
        MovimentacaoEstoqueResponseDTO result = movimentacaoEstoqueService.registrarEntrada(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(TipoMovimentacao.ENTRADA, result.getTipoMovimentacao());
        verify(movimentacaoEstoqueRepository, times(1)).save(any(MovimentacaoEstoque.class));
        verify(produtoEstoqueService, times(1)).atualizarSaldoAposMovimentacao(100L);
    }

    @Test
    @DisplayName("Deve registrar saída de estoque")
    void deveRegistrarSaidaDeEstoque() {
        // Arrange
        MovimentacaoEstoqueRequestDTO request = new MovimentacaoEstoqueRequestDTO();
        request.setProdutoCatalogoId(100L);
        request.setTipoMovimentacao(TipoMovimentacao.SAIDA);
        request.setQuantidade(10);

        MovimentacaoEstoque entity = new MovimentacaoEstoque();
        entity.setId(2L);
        entity.setProdutoCatalogoId(100L);
        entity.setTipoMovimentacao(TipoMovimentacao.SAIDA);

        MovimentacaoEstoqueResponseDTO response = new MovimentacaoEstoqueResponseDTO();
        response.setId(2L);
        response.setTipoMovimentacao(TipoMovimentacao.SAIDA);

        when(movimentacaoEstoqueMapper.toEntity(request)).thenReturn(entity);
        when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class))).thenReturn(entity);
        when(movimentacaoEstoqueMapper.toDTO(entity)).thenReturn(response);
        doNothing().when(produtoEstoqueService).atualizarSaldoAposMovimentacao(any());

        // Act
        MovimentacaoEstoqueResponseDTO result = movimentacaoEstoqueService.registrarSaida(request);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(TipoMovimentacao.SAIDA, result.getTipoMovimentacao());
        verify(movimentacaoEstoqueRepository, times(1)).save(any(MovimentacaoEstoque.class));
        verify(produtoEstoqueService, times(1)).atualizarSaldoAposMovimentacao(100L);
    }

    @Test
    @DisplayName("Deve listar todas as movimentações sem filtros")
    void deveListarTodasMovimentacoesSemFiltros() {
        // Arrange
        MovimentacaoEstoque mov1 = new MovimentacaoEstoque();
        mov1.setId(1L);
        mov1.setTipoMovimentacao(TipoMovimentacao.ENTRADA);

        MovimentacaoEstoque mov2 = new MovimentacaoEstoque();
        mov2.setId(2L);
        mov2.setTipoMovimentacao(TipoMovimentacao.SAIDA);

        List<MovimentacaoEstoque> movimentacoes = Arrays.asList(mov1, mov2);

        MovimentacaoEstoqueResponseDTO dto1 = new MovimentacaoEstoqueResponseDTO();
        dto1.setId(1L);

        MovimentacaoEstoqueResponseDTO dto2 = new MovimentacaoEstoqueResponseDTO();
        dto2.setId(2L);

        when(movimentacaoEstoqueRepository.findAll()).thenReturn(movimentacoes);
        when(movimentacaoEstoqueMapper.toDTO(mov1)).thenReturn(dto1);
        when(movimentacaoEstoqueMapper.toDTO(mov2)).thenReturn(dto2);

        // Act
        List<MovimentacaoEstoqueResponseDTO> result = movimentacaoEstoqueService.listarMovimentacoes(null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(movimentacaoEstoqueRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve listar movimentações filtradas por produto")
    void deveListarMovimentacoesFiltradasPorProduto() {
        // Arrange
        Long produtoCatalogoId = 100L;
        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setId(1L);
        mov.setProdutoCatalogoId(produtoCatalogoId);

        MovimentacaoEstoqueResponseDTO dto = new MovimentacaoEstoqueResponseDTO();
        dto.setId(1L);

        when(movimentacaoEstoqueRepository.findByProdutoCatalogoId(produtoCatalogoId))
                .thenReturn(Arrays.asList(mov));
        when(movimentacaoEstoqueMapper.toDTO(mov)).thenReturn(dto);

        // Act
        List<MovimentacaoEstoqueResponseDTO> result = movimentacaoEstoqueService.listarMovimentacoes(
                produtoCatalogoId, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(movimentacaoEstoqueRepository, times(1)).findByProdutoCatalogoId(produtoCatalogoId);
    }

    @Test
    @DisplayName("Deve buscar movimentação por ID")
    void deveBuscarMovimentacaoPorId() {
        // Arrange
        Long id = 1L;
        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setId(id);
        mov.setTipoMovimentacao(TipoMovimentacao.ENTRADA);

        MovimentacaoEstoqueResponseDTO dto = new MovimentacaoEstoqueResponseDTO();
        dto.setId(id);

        when(movimentacaoEstoqueRepository.findById(id)).thenReturn(Optional.of(mov));
        when(movimentacaoEstoqueMapper.toDTO(mov)).thenReturn(dto);

        // Act
        MovimentacaoEstoqueResponseDTO result = movimentacaoEstoqueService.buscarPorId(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(movimentacaoEstoqueRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando movimentação não encontrada")
    void deveLancarExcecaoQuandoMovimentacaoNaoEncontrada() {
        // Arrange
        Long id = 999L;
        when(movimentacaoEstoqueRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, 
                () -> movimentacaoEstoqueService.buscarPorId(id));
        verify(movimentacaoEstoqueRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve listar movimentações por tipo")
    void deveListarMovimentacoesPorTipo() {
        // Arrange
        TipoMovimentacao tipo = TipoMovimentacao.ENTRADA;
        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setId(1L);
        mov.setTipoMovimentacao(tipo);

        MovimentacaoEstoqueResponseDTO dto = new MovimentacaoEstoqueResponseDTO();
        dto.setId(1L);

        when(movimentacaoEstoqueRepository.findByTipoMovimentacao(tipo))
                .thenReturn(Arrays.asList(mov));
        when(movimentacaoEstoqueMapper.toDTO(mov)).thenReturn(dto);

        // Act
        List<MovimentacaoEstoqueResponseDTO> result = movimentacaoEstoqueService.listarMovimentacoes(
                null, tipo, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(movimentacaoEstoqueRepository, times(1)).findByTipoMovimentacao(tipo);
    }
}
