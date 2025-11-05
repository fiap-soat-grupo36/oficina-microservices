package br.com.fiap.oficina.inventory.controller;

import br.com.fiap.oficina.inventory.dto.response.ProdutoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.service.ProdutoEstoqueService;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoEstoqueController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ProdutoEstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProdutoEstoqueService produtoEstoqueService;

    @Test
    @DisplayName("Deve listar todos os produtos em estoque")
    void deveListarTodosProdutosEmEstoque() throws Exception {
        // Arrange
        ProdutoEstoqueResponseDTO produto1 = new ProdutoEstoqueResponseDTO();
        produto1.setId(1L);
        produto1.setProdutoCatalogoId(100L);
        produto1.setQuantidadeTotal(50);
        produto1.setQuantidadeDisponivel(45);

        ProdutoEstoqueResponseDTO produto2 = new ProdutoEstoqueResponseDTO();
        produto2.setId(2L);
        produto2.setProdutoCatalogoId(200L);
        produto2.setQuantidadeTotal(30);
        produto2.setQuantidadeDisponivel(28);

        List<ProdutoEstoqueResponseDTO> produtos = Arrays.asList(produto1, produto2);

        when(produtoEstoqueService.listarTodos()).thenReturn(produtos);

        // Act & Assert
        mockMvc.perform(get("/estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(produtoEstoqueService, times(1)).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar 204 quando lista vazia")
    void deveRetornar204QuandoListaVazia() throws Exception {
        // Arrange
        when(produtoEstoqueService.listarTodos()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/estoque"))
                .andExpect(status().isNoContent());

        verify(produtoEstoqueService, times(1)).listarTodos();
    }

    @Test
    @DisplayName("Deve buscar produto em estoque por ID")
    void deveBuscarProdutoPorId() throws Exception {
        // Arrange
        ProdutoEstoqueResponseDTO produto = new ProdutoEstoqueResponseDTO();
        produto.setId(1L);
        produto.setProdutoCatalogoId(100L);
        produto.setQuantidadeTotal(50);
        produto.setQuantidadeDisponivel(45);

        when(produtoEstoqueService.buscarPorId(1L)).thenReturn(produto);

        // Act & Assert
        mockMvc.perform(get("/estoque/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.produtoCatalogoId").value(100));

        verify(produtoEstoqueService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar 404 quando produto não encontrado")
    void deveRetornar404QuandoProdutoNaoEncontrado() throws Exception {
        // Arrange
        when(produtoEstoqueService.buscarPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Produto não encontrado"));

        // Act & Assert
        mockMvc.perform(get("/estoque/999"))
                .andExpect(status().isNotFound());

        verify(produtoEstoqueService, times(1)).buscarPorId(999L);
    }

    @Test
    @DisplayName("Deve buscar estoque por produto do catálogo")
    void deveBuscarPorProdutoCatalogo() throws Exception {
        // Arrange
        ProdutoEstoqueResponseDTO produto = new ProdutoEstoqueResponseDTO();
        produto.setId(1L);
        produto.setProdutoCatalogoId(100L);
        produto.setQuantidadeTotal(50);

        when(produtoEstoqueService.buscarPorProdutoCatalogo(100L)).thenReturn(produto);

        // Act & Assert
        mockMvc.perform(get("/estoque/produto/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoCatalogoId").value(100));

        verify(produtoEstoqueService, times(1)).buscarPorProdutoCatalogo(100L);
    }

    @Test
    @DisplayName("Deve buscar produtos por termo")
    void deveBuscarProdutosPorTermo() throws Exception {
        // Arrange
        ProdutoEstoqueResponseDTO produto = new ProdutoEstoqueResponseDTO();
        produto.setId(1L);
        produto.setProdutoCatalogoId(100L);

        when(produtoEstoqueService.buscarPorTermo("filtro")).thenReturn(Arrays.asList(produto));

        // Act & Assert
        mockMvc.perform(get("/estoque/buscar").param("termo", "filtro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(produtoEstoqueService, times(1)).buscarPorTermo("filtro");
    }

    @Test
    @DisplayName("Deve listar produtos com baixo estoque")
    void deveListarProdutosComBaixoEstoque() throws Exception {
        // Arrange
        ProdutoEstoqueResponseDTO produto = new ProdutoEstoqueResponseDTO();
        produto.setId(1L);
        produto.setProdutoCatalogoId(100L);
        produto.setQuantidadeTotal(5);
        produto.setEstoqueMinimo(10);

        when(produtoEstoqueService.listarBaixoEstoque()).thenReturn(Arrays.asList(produto));

        // Act & Assert
        mockMvc.perform(get("/estoque/baixo-estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].quantidadeTotal").value(5));

        verify(produtoEstoqueService, times(1)).listarBaixoEstoque();
    }

    @Test
    @DisplayName("Deve atualizar estoque mínimo")
    void deveAtualizarEstoqueMinimo() throws Exception {
        // Arrange
        ProdutoEstoqueResponseDTO produto = new ProdutoEstoqueResponseDTO();
        produto.setId(1L);
        produto.setEstoqueMinimo(20);

        when(produtoEstoqueService.atualizarEstoqueMinimo(1L, 20)).thenReturn(produto);

        // Act & Assert
        mockMvc.perform(put("/estoque/1").param("estoqueMinimo", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estoqueMinimo").value(20));

        verify(produtoEstoqueService, times(1)).atualizarEstoqueMinimo(1L, 20);
    }

    @Test
    @DisplayName("Deve obter saldo consolidado (endpoint legado)")
    void deveObterSaldoConsolidado() throws Exception {
        // Arrange
        ProdutoEstoqueResponseDTO saldo = new ProdutoEstoqueResponseDTO();
        saldo.setId(1L);
        saldo.setProdutoCatalogoId(100L);
        saldo.setQuantidadeTotal(50);
        saldo.setQuantidadeDisponivel(45);

        when(produtoEstoqueService.getSaldoConsolidado(100L)).thenReturn(saldo);

        // Act & Assert
        mockMvc.perform(get("/estoque/saldo/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoCatalogoId").value(100))
                .andExpect(jsonPath("$.quantidadeTotal").value(50));

        verify(produtoEstoqueService, times(1)).getSaldoConsolidado(100L);
    }
}
