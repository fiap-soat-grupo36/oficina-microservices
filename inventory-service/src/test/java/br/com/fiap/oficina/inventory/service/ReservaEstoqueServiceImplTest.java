package br.com.fiap.oficina.inventory.service;

import br.com.fiap.oficina.inventory.entity.ProdutoEstoque;
import br.com.fiap.oficina.inventory.entity.ReservaEstoque;
import br.com.fiap.oficina.inventory.repository.ReservaEstoqueRepository;
import br.com.fiap.oficina.inventory.service.impl.ReservaEstoqueServiceImpl;
import br.com.fiap.oficina.shared.exception.EstoqueInsuficienteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaEstoqueServiceImplTest {

    @Mock
    private ReservaEstoqueRepository reservaEstoqueRepository;

    @Mock
    private ProdutoEstoqueService produtoEstoqueService;

    @InjectMocks
    private ReservaEstoqueServiceImpl reservaEstoqueService;

    @Test
    @DisplayName("Deve reservar estoque com sucesso")
    void deveReservarEstoqueComSucesso() {
        // Arrange
        Long produtoCatalogoId = 100L;
        Long ordemServicoId = 1L;
        Integer quantidade = 10;

        ProdutoEstoque produtoEstoque = new ProdutoEstoque();
        produtoEstoque.setProdutoCatalogoId(produtoCatalogoId);
        produtoEstoque.setQuantidadeDisponivel(50);

        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setId(1L);
        reserva.setProdutoCatalogoId(produtoCatalogoId);
        reserva.setOrdemServicoId(ordemServicoId);
        reserva.setQuantidadeReservada(quantidade);
        reserva.setAtiva(true);

        when(produtoEstoqueService.obterOuCriarSaldo(produtoCatalogoId))
                .thenReturn(produtoEstoque);
        when(reservaEstoqueRepository.save(any(ReservaEstoque.class)))
                .thenReturn(reserva);
        doNothing().when(produtoEstoqueService).atualizarSaldoAposMovimentacao(any());

        // Act
        ReservaEstoque result = reservaEstoqueService.reservar(produtoCatalogoId, ordemServicoId, quantidade);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(quantidade, result.getQuantidadeReservada());
        assertTrue(result.getAtiva());
        verify(reservaEstoqueRepository, times(1)).save(any(ReservaEstoque.class));
        verify(produtoEstoqueService, times(1)).atualizarSaldoAposMovimentacao(produtoCatalogoId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque insuficiente")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        // Arrange
        Long produtoCatalogoId = 100L;
        Long ordemServicoId = 1L;
        Integer quantidade = 100;

        ProdutoEstoque produtoEstoque = new ProdutoEstoque();
        produtoEstoque.setProdutoCatalogoId(produtoCatalogoId);
        produtoEstoque.setQuantidadeDisponivel(50);

        when(produtoEstoqueService.obterOuCriarSaldo(produtoCatalogoId))
                .thenReturn(produtoEstoque);

        // Act & Assert
        assertThrows(EstoqueInsuficienteException.class,
                () -> reservaEstoqueService.reservar(produtoCatalogoId, ordemServicoId, quantidade));
        verify(reservaEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve cancelar reservas por ordem de serviço")
    void deveCancelarReservasPorOrdemServico() {
        // Arrange
        Long ordemServicoId = 1L;
        
        ReservaEstoque reserva1 = new ReservaEstoque();
        reserva1.setId(1L);
        reserva1.setOrdemServicoId(ordemServicoId);
        reserva1.setProdutoCatalogoId(100L);
        reserva1.setAtiva(true);

        ReservaEstoque reserva2 = new ReservaEstoque();
        reserva2.setId(2L);
        reserva2.setOrdemServicoId(ordemServicoId);
        reserva2.setProdutoCatalogoId(200L);
        reserva2.setAtiva(true);

        List<ReservaEstoque> reservas = Arrays.asList(reserva1, reserva2);

        when(reservaEstoqueRepository.findByOrdemServicoIdAndAtivaTrue(ordemServicoId))
                .thenReturn(reservas);
        when(reservaEstoqueRepository.save(any(ReservaEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(produtoEstoqueService).atualizarSaldoAposMovimentacao(any());

        // Act
        reservaEstoqueService.cancelarPorOrdemServico(ordemServicoId);

        // Assert
        assertFalse(reserva1.getAtiva());
        assertFalse(reserva2.getAtiva());
        verify(reservaEstoqueRepository, times(2)).save(any(ReservaEstoque.class));
        verify(produtoEstoqueService, times(1)).atualizarSaldoAposMovimentacao(100L);
        verify(produtoEstoqueService, times(1)).atualizarSaldoAposMovimentacao(200L);
    }

    @Test
    @DisplayName("Deve listar reservas por ordem de serviço")
    void deveListarReservasPorOrdemServico() {
        // Arrange
        Long ordemServicoId = 1L;
        
        ReservaEstoque reserva1 = new ReservaEstoque();
        reserva1.setId(1L);
        reserva1.setOrdemServicoId(ordemServicoId);

        ReservaEstoque reserva2 = new ReservaEstoque();
        reserva2.setId(2L);
        reserva2.setOrdemServicoId(ordemServicoId);

        List<ReservaEstoque> reservas = Arrays.asList(reserva1, reserva2);

        when(reservaEstoqueRepository.findByOrdemServicoId(ordemServicoId))
                .thenReturn(reservas);

        // Act
        List<ReservaEstoque> result = reservaEstoqueService.listarReservasPorOrdemServico(ordemServicoId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(reservaEstoqueRepository, times(1)).findByOrdemServicoId(ordemServicoId);
    }

    @Test
    @DisplayName("Deve reservar corretamente com estoque exato")
    void deveReservarCorretamenteComEstoqueExato() {
        // Arrange
        Long produtoCatalogoId = 100L;
        Long ordemServicoId = 1L;
        Integer quantidade = 50;

        ProdutoEstoque produtoEstoque = new ProdutoEstoque();
        produtoEstoque.setProdutoCatalogoId(produtoCatalogoId);
        produtoEstoque.setQuantidadeDisponivel(50);

        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setProdutoCatalogoId(produtoCatalogoId);
        reserva.setQuantidadeReservada(quantidade);

        when(produtoEstoqueService.obterOuCriarSaldo(produtoCatalogoId))
                .thenReturn(produtoEstoque);
        when(reservaEstoqueRepository.save(any(ReservaEstoque.class)))
                .thenReturn(reserva);
        doNothing().when(produtoEstoqueService).atualizarSaldoAposMovimentacao(any());

        // Act
        ReservaEstoque result = reservaEstoqueService.reservar(produtoCatalogoId, ordemServicoId, quantidade);

        // Assert
        assertNotNull(result);
        assertEquals(quantidade, result.getQuantidadeReservada());
        verify(reservaEstoqueRepository, times(1)).save(any(ReservaEstoque.class));
    }
}
