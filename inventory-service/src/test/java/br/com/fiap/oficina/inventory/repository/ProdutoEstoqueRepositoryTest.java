package br.com.fiap.oficina.inventory.repository;

import br.com.fiap.oficina.inventory.entity.ProdutoEstoque;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class ProdutoEstoqueRepositoryTest {

    @Autowired
    private ProdutoEstoqueRepository produtoEstoqueRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve buscar produto por ID de catálogo")
    void deveBuscarProdutoPorIdCatalogo() {
        // Arrange
        ProdutoEstoque produto = new ProdutoEstoque();
        produto.setProdutoCatalogoId(100L);
        produto.setQuantidadeTotal(50);
        produto.setQuantidadeDisponivel(45);
        produto.setPrecoCustoMedio(BigDecimal.TEN);
        entityManager.persist(produto);
        entityManager.flush();

        // Act
        Optional<ProdutoEstoque> result = produtoEstoqueRepository.findByProdutoCatalogoId(100L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getProdutoCatalogoId());
        assertEquals(50, result.get().getQuantidadeTotal());
    }

    @Test
    @DisplayName("Deve retornar vazio quando produto catálogo não existe")
    void deveRetornarVazioQuandoProdutoCatalogoNaoExiste() {
        // Act
        Optional<ProdutoEstoque> result = produtoEstoqueRepository.findByProdutoCatalogoId(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Deve buscar produtos com baixo estoque")
    void deveBuscarProdutosComBaixoEstoque() {
        // Arrange
        ProdutoEstoque produto1 = new ProdutoEstoque();
        produto1.setProdutoCatalogoId(100L);
        produto1.setQuantidadeTotal(5);
        produto1.setQuantidadeDisponivel(5);
        produto1.setEstoqueMinimo(10);

        ProdutoEstoque produto2 = new ProdutoEstoque();
        produto2.setProdutoCatalogoId(200L);
        produto2.setQuantidadeTotal(50);
        produto2.setQuantidadeDisponivel(50);
        produto2.setEstoqueMinimo(10);

        entityManager.persist(produto1);
        entityManager.persist(produto2);
        entityManager.flush();

        // Act
        List<ProdutoEstoque> result = produtoEstoqueRepository.findBaixoEstoque();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getProdutoCatalogoId());
    }

    @Test
    @DisplayName("Deve salvar novo produto em estoque")
    void deveSalvarNovoProdutoEmEstoque() {
        // Arrange
        ProdutoEstoque produto = new ProdutoEstoque();
        produto.setProdutoCatalogoId(300L);
        produto.setQuantidadeTotal(100);
        produto.setQuantidadeDisponivel(95);
        produto.setQuantidadeReservada(5);
        produto.setPrecoCustoMedio(BigDecimal.valueOf(15.50));
        produto.setEstoqueMinimo(10);

        // Act
        ProdutoEstoque saved = produtoEstoqueRepository.save(produto);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(300L, saved.getProdutoCatalogoId());
        assertEquals(100, saved.getQuantidadeTotal());
    }

    @Test
    @DisplayName("Deve buscar produto por ID de catálogo com lock pessimista")
    void deveBuscarProdutoPorIdCatalogoComLock() {
        // Arrange
        ProdutoEstoque produto = new ProdutoEstoque();
        produto.setProdutoCatalogoId(400L);
        produto.setQuantidadeTotal(30);
        entityManager.persist(produto);
        entityManager.flush();

        // Act
        Optional<ProdutoEstoque> result = produtoEstoqueRepository.findByProdutoCatalogoIdForUpdate(400L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(400L, result.get().getProdutoCatalogoId());
    }

    @Test
    @DisplayName("Deve atualizar quantidade do produto")
    void deveAtualizarQuantidadeDoProduto() {
        // Arrange
        ProdutoEstoque produto = new ProdutoEstoque();
        produto.setProdutoCatalogoId(500L);
        produto.setQuantidadeTotal(50);
        produto.setQuantidadeDisponivel(50);
        ProdutoEstoque saved = entityManager.persist(produto);
        entityManager.flush();

        // Act
        saved.setQuantidadeTotal(75);
        saved.setQuantidadeDisponivel(70);
        produtoEstoqueRepository.save(saved);
        entityManager.flush();

        // Assert
        ProdutoEstoque updated = produtoEstoqueRepository.findById(saved.getId()).orElseThrow();
        assertEquals(75, updated.getQuantidadeTotal());
        assertEquals(70, updated.getQuantidadeDisponivel());
    }
}
