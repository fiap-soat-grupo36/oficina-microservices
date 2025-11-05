package br.com.fiap.oficina.inventory.repository;

import br.com.fiap.oficina.inventory.entity.MovimentacaoEstoque;
import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class MovimentacaoEstoqueRepositoryTest {

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve buscar movimentações por produto catálogo")
    void deveBuscarMovimentacoesPorProdutoCatalogo() {
        // Arrange
        MovimentacaoEstoque mov1 = new MovimentacaoEstoque();
        mov1.setProdutoCatalogoId(100L);
        mov1.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        mov1.setQuantidade(50);
        mov1.setPrecoUnitario(BigDecimal.TEN);

        MovimentacaoEstoque mov2 = new MovimentacaoEstoque();
        mov2.setProdutoCatalogoId(100L);
        mov2.setTipoMovimentacao(TipoMovimentacao.SAIDA);
        mov2.setQuantidade(10);

        MovimentacaoEstoque mov3 = new MovimentacaoEstoque();
        mov3.setProdutoCatalogoId(200L);
        mov3.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        mov3.setQuantidade(30);

        entityManager.persist(mov1);
        entityManager.persist(mov2);
        entityManager.persist(mov3);
        entityManager.flush();

        // Act
        List<MovimentacaoEstoque> result = movimentacaoEstoqueRepository.findByProdutoCatalogoId(100L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(m -> m.getProdutoCatalogoId().equals(100L)));
    }

    @Test
    @DisplayName("Deve buscar movimentações por tipo")
    void deveBuscarMovimentacoesPorTipo() {
        // Arrange
        MovimentacaoEstoque entrada1 = new MovimentacaoEstoque();
        entrada1.setProdutoCatalogoId(100L);
        entrada1.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        entrada1.setQuantidade(50);
        entrada1.setPrecoUnitario(BigDecimal.TEN);

        MovimentacaoEstoque entrada2 = new MovimentacaoEstoque();
        entrada2.setProdutoCatalogoId(200L);
        entrada2.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        entrada2.setQuantidade(30);
        entrada2.setPrecoUnitario(BigDecimal.valueOf(15));

        MovimentacaoEstoque saida = new MovimentacaoEstoque();
        saida.setProdutoCatalogoId(100L);
        saida.setTipoMovimentacao(TipoMovimentacao.SAIDA);
        saida.setQuantidade(10);

        entityManager.persist(entrada1);
        entityManager.persist(entrada2);
        entityManager.persist(saida);
        entityManager.flush();

        // Act
        List<MovimentacaoEstoque> result = movimentacaoEstoqueRepository.findByTipoMovimentacao(TipoMovimentacao.ENTRADA);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(m -> m.getTipoMovimentacao() == TipoMovimentacao.ENTRADA));
    }

    @Test
    @DisplayName("Deve buscar movimentações com filtros combinados")
    void deveBuscarMovimentacoesComFiltrosCombinados() {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();
        
        MovimentacaoEstoque mov1 = new MovimentacaoEstoque();
        mov1.setProdutoCatalogoId(100L);
        mov1.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        mov1.setQuantidade(50);
        mov1.setDataMovimentacao(agora);
        mov1.setPrecoUnitario(BigDecimal.TEN);

        MovimentacaoEstoque mov2 = new MovimentacaoEstoque();
        mov2.setProdutoCatalogoId(100L);
        mov2.setTipoMovimentacao(TipoMovimentacao.SAIDA);
        mov2.setQuantidade(10);
        mov2.setDataMovimentacao(agora.minusDays(1));

        entityManager.persist(mov1);
        entityManager.persist(mov2);
        entityManager.flush();

        // Act
        List<MovimentacaoEstoque> result = movimentacaoEstoqueRepository.findWithFilters(
                100L, TipoMovimentacao.ENTRADA, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TipoMovimentacao.ENTRADA, result.get(0).getTipoMovimentacao());
    }

    @Test
    @DisplayName("Deve buscar movimentações por intervalo de datas")
    void deveBuscarMovimentacoesPorIntervaloDeDatas() {
        // Arrange
        LocalDateTime dataInicio = LocalDateTime.now().minusDays(5);
        LocalDateTime dataFim = LocalDateTime.now().minusDays(1);

        MovimentacaoEstoque mov1 = new MovimentacaoEstoque();
        mov1.setProdutoCatalogoId(100L);
        mov1.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        mov1.setQuantidade(50);
        mov1.setDataMovimentacao(LocalDateTime.now().minusDays(3));
        mov1.setPrecoUnitario(BigDecimal.TEN);

        MovimentacaoEstoque mov2 = new MovimentacaoEstoque();
        mov2.setProdutoCatalogoId(100L);
        mov2.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        mov2.setQuantidade(30);
        mov2.setDataMovimentacao(LocalDateTime.now().minusDays(10));
        mov2.setPrecoUnitario(BigDecimal.TEN);

        entityManager.persist(mov1);
        entityManager.persist(mov2);
        entityManager.flush();

        // Act
        List<MovimentacaoEstoque> result = movimentacaoEstoqueRepository.findWithFilters(
                null, null, dataInicio, dataFim);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Deve salvar nova movimentação de estoque")
    void deveSalvarNovaMovimentacaoDeEstoque() {
        // Arrange
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProdutoCatalogoId(300L);
        movimentacao.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        movimentacao.setQuantidade(100);
        movimentacao.setPrecoUnitario(BigDecimal.valueOf(25.50));
        movimentacao.setObservacao("Entrada inicial");
        movimentacao.setNumeroNotaFiscal("NF123456");

        // Act
        MovimentacaoEstoque saved = movimentacaoEstoqueRepository.save(movimentacao);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(300L, saved.getProdutoCatalogoId());
        assertEquals(100, saved.getQuantidade());
        assertEquals("NF123456", saved.getNumeroNotaFiscal());
    }
}
