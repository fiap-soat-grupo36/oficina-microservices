package br.com.fiap.oficina.inventory.repository;

import br.com.fiap.oficina.inventory.entity.ReservaEstoque;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class ReservaEstoqueRepositoryTest {

    @Autowired
    private ReservaEstoqueRepository reservaEstoqueRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve buscar reservas ativas por ordem de serviço")
    void deveBuscarReservasAtivasPorOrdemServico() {
        // Arrange
        ReservaEstoque reserva1 = new ReservaEstoque();
        reserva1.setProdutoCatalogoId(100L);
        reserva1.setOrdemServicoId(1L);
        reserva1.setQuantidadeReservada(10);
        reserva1.setAtiva(true);

        ReservaEstoque reserva2 = new ReservaEstoque();
        reserva2.setProdutoCatalogoId(200L);
        reserva2.setOrdemServicoId(1L);
        reserva2.setQuantidadeReservada(5);
        reserva2.setAtiva(true);

        ReservaEstoque reserva3 = new ReservaEstoque();
        reserva3.setProdutoCatalogoId(300L);
        reserva3.setOrdemServicoId(1L);
        reserva3.setQuantidadeReservada(3);
        reserva3.setAtiva(false);

        entityManager.persist(reserva1);
        entityManager.persist(reserva2);
        entityManager.persist(reserva3);
        entityManager.flush();

        // Act
        List<ReservaEstoque> result = reservaEstoqueRepository.findByOrdemServicoIdAndAtivaTrue(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(ReservaEstoque::getAtiva));
    }

    @Test
    @DisplayName("Deve buscar reservas ativas por produto catálogo")
    void deveBuscarReservasAtivasPorProdutoCatalogo() {
        // Arrange
        ReservaEstoque reserva1 = new ReservaEstoque();
        reserva1.setProdutoCatalogoId(100L);
        reserva1.setOrdemServicoId(1L);
        reserva1.setQuantidadeReservada(10);
        reserva1.setAtiva(true);

        ReservaEstoque reserva2 = new ReservaEstoque();
        reserva2.setProdutoCatalogoId(100L);
        reserva2.setOrdemServicoId(2L);
        reserva2.setQuantidadeReservada(5);
        reserva2.setAtiva(true);

        ReservaEstoque reserva3 = new ReservaEstoque();
        reserva3.setProdutoCatalogoId(100L);
        reserva3.setOrdemServicoId(3L);
        reserva3.setQuantidadeReservada(3);
        reserva3.setAtiva(false);

        entityManager.persist(reserva1);
        entityManager.persist(reserva2);
        entityManager.persist(reserva3);
        entityManager.flush();

        // Act
        List<ReservaEstoque> result = reservaEstoqueRepository.findByProdutoCatalogoIdAndAtivaTrue(100L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getProdutoCatalogoId().equals(100L) && r.getAtiva()));
    }

    @Test
    @DisplayName("Deve listar todas as reservas por ordem de serviço")
    void deveListarTodasReservasPorOrdemServico() {
        // Arrange
        ReservaEstoque reserva1 = new ReservaEstoque();
        reserva1.setProdutoCatalogoId(100L);
        reserva1.setOrdemServicoId(5L);
        reserva1.setQuantidadeReservada(10);
        reserva1.setAtiva(true);

        ReservaEstoque reserva2 = new ReservaEstoque();
        reserva2.setProdutoCatalogoId(200L);
        reserva2.setOrdemServicoId(5L);
        reserva2.setQuantidadeReservada(5);
        reserva2.setAtiva(false);

        entityManager.persist(reserva1);
        entityManager.persist(reserva2);
        entityManager.flush();

        // Act
        List<ReservaEstoque> result = reservaEstoqueRepository.listarReservasProdutosPorOS(5L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getOrdemServicoId().equals(5L)));
    }

    @Test
    @DisplayName("Deve salvar nova reserva de estoque")
    void deveSalvarNovaReservaDeEstoque() {
        // Arrange
        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setProdutoCatalogoId(300L);
        reserva.setOrdemServicoId(10L);
        reserva.setQuantidadeReservada(25);
        reserva.setAtiva(true);

        // Act
        ReservaEstoque saved = reservaEstoqueRepository.save(reserva);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(300L, saved.getProdutoCatalogoId());
        assertEquals(10L, saved.getOrdemServicoId());
        assertEquals(25, saved.getQuantidadeReservada());
        assertTrue(saved.getAtiva());
    }

    @Test
    @DisplayName("Deve atualizar status da reserva")
    void deveAtualizarStatusDaReserva() {
        // Arrange
        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setProdutoCatalogoId(400L);
        reserva.setOrdemServicoId(15L);
        reserva.setQuantidadeReservada(15);
        reserva.setAtiva(true);
        ReservaEstoque saved = entityManager.persist(reserva);
        entityManager.flush();

        // Act
        saved.setAtiva(false);
        reservaEstoqueRepository.save(saved);
        entityManager.flush();

        // Assert
        ReservaEstoque updated = reservaEstoqueRepository.findById(saved.getId()).orElseThrow();
        assertFalse(updated.getAtiva());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há reservas ativas")
    void deveRetornarListaVaziaQuandoNaoHaReservasAtivas() {
        // Act
        List<ReservaEstoque> result = reservaEstoqueRepository.findByOrdemServicoIdAndAtivaTrue(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
