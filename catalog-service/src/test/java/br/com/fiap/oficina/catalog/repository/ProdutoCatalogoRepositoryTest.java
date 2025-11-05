package br.com.fiap.oficina.catalog.repository;

import br.com.fiap.oficina.catalog.entity.ProdutoCatalogo;
import br.com.fiap.oficina.shared.enums.CategoriaProduto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class ProdutoCatalogoRepositoryTest {

    @Autowired
    private ProdutoCatalogoRepository produtoCatalogoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve buscar produtos ativos")
    void deveBuscarProdutosAtivos() {
        // Arrange
        ProdutoCatalogo ativo = new ProdutoCatalogo();
        ativo.setNome("Produto Ativo");
        ativo.setDescricao("Descrição");
        ativo.setCategoria(CategoriaProduto.INSUMO);
        ativo.setPreco(BigDecimal.valueOf(100.00));
        ativo.setAtivo(true);

        ProdutoCatalogo inativo = new ProdutoCatalogo();
        inativo.setNome("Produto Inativo");
        inativo.setDescricao("Descrição");
        inativo.setCategoria(CategoriaProduto.INSUMO);
        inativo.setPreco(BigDecimal.valueOf(100.00));
        inativo.setAtivo(false);

        entityManager.persist(ativo);
        entityManager.persist(inativo);
        entityManager.flush();

        // Act
        List<ProdutoCatalogo> result = produtoCatalogoRepository.findByAtivoTrue();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getAtivo());
    }

    @Test
    @DisplayName("Deve buscar produtos inativos")
    void deveBuscarProdutosInativos() {
        // Arrange
        ProdutoCatalogo ativo = new ProdutoCatalogo();
        ativo.setNome("Produto Ativo");
        ativo.setDescricao("Descrição");
        ativo.setCategoria(CategoriaProduto.INSUMO);

        ativo.setPreco(BigDecimal.valueOf(100.00));
        ativo.setAtivo(true);

        ProdutoCatalogo inativo = new ProdutoCatalogo();
        inativo.setNome("Produto Inativo");
        inativo.setDescricao("Descrição");
        inativo.setCategoria(CategoriaProduto.INSUMO);

        inativo.setPreco(BigDecimal.valueOf(100.00));
        inativo.setAtivo(false);

        entityManager.persist(ativo);
        entityManager.persist(inativo);
        entityManager.flush();

        // Act
        List<ProdutoCatalogo> result = produtoCatalogoRepository.findByAtivoFalse();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getAtivo());
    }

    @Test
    @DisplayName("Deve buscar produtos por termo no nome")
    void deveBuscarProdutosPorTermoNoNome() {
        // Arrange
        ProdutoCatalogo produto = new ProdutoCatalogo();
        produto.setNome("Filtro de Óleo");
        produto.setDescricao("Filtro para motor");
        produto.setCategoria(br.com.fiap.oficina.shared.enums.CategoriaProduto.PECA);
        produto.setPreco(BigDecimal.valueOf(50.00));
        produto.setAtivo(true);

        entityManager.persist(produto);
        entityManager.flush();

        // Act
        List<ProdutoCatalogo> result = produtoCatalogoRepository.buscarPorTermo("filtro");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getNome().toLowerCase().contains("filtro"));
    }

    @Test
    @DisplayName("Deve buscar produtos por termo na descrição")
    void deveBuscarProdutosPorTermoNaDescricao() {
        // Arrange
        ProdutoCatalogo produto = new ProdutoCatalogo();
        produto.setNome("Produto Especial");
        produto.setDescricao("Filtro de alta performance");
        produto.setCategoria(br.com.fiap.oficina.shared.enums.CategoriaProduto.PECA);
        produto.setPreco(BigDecimal.valueOf(50.00));
        produto.setAtivo(true);

        entityManager.persist(produto);
        entityManager.flush();

        // Act
        List<ProdutoCatalogo> result = produtoCatalogoRepository.buscarPorTermo("filtro");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getDescricao().toLowerCase().contains("filtro"));
    }

    @Test
    @DisplayName("Não deve buscar produtos inativos por termo")
    void naoDeveBuscarProdutosInativosPorTermo() {
        // Arrange
        ProdutoCatalogo produto = new ProdutoCatalogo();
        produto.setNome("Óleo Motor Inativo");
        produto.setDescricao("Óleo sintético 5W30");
        produto.setCategoria(CategoriaProduto.INSUMO);
        produto.setPreco(BigDecimal.valueOf(45.90));
        produto.setAtivo(false); // Produto inativo

        entityManager.persistAndFlush(produto);

        // Act
        List<ProdutoCatalogo> resultado = produtoCatalogoRepository.buscarPorTermo("óleo");

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty(), "Não deve retornar produtos inativos");
    }
}
