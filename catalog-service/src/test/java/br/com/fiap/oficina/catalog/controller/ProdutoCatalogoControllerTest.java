package br.com.fiap.oficina.catalog.controller;

import br.com.fiap.oficina.catalog.dto.request.ProdutoCatalogoRequestDTO;
import br.com.fiap.oficina.catalog.dto.response.ProdutoCatalogoResponseDTO;
import br.com.fiap.oficina.catalog.service.ProdutoCatalogoService;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

@WebMvcTest(ProdutoCatalogoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ProdutoCatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProdutoCatalogoService produtoCatalogoService;

    @Test
    @DisplayName("Deve cadastrar produto com sucesso")
    void deveCadastrarProdutoComSucesso() throws Exception {
        // Arrange
        ProdutoCatalogoRequestDTO request = new ProdutoCatalogoRequestDTO();
        request.setNome("Filtro de Óleo");
        request.setDescricao("Filtro de óleo para motor");
        request.setCategoria(br.com.fiap.oficina.shared.enums.CategoriaProduto.PECA);
        request.setPreco(BigDecimal.valueOf(45.00));

        ProdutoCatalogoResponseDTO response = new ProdutoCatalogoResponseDTO();
        response.setId(1L);
        response.setNome("Filtro de Óleo");

        when(produtoCatalogoService.salvar(any(ProdutoCatalogoRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/catalogo-produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(produtoCatalogoService, times(1)).salvar(any(ProdutoCatalogoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    void deveListarTodosOsProdutos() throws Exception {
        // Arrange
        ProdutoCatalogoResponseDTO produto1 = new ProdutoCatalogoResponseDTO();
        produto1.setId(1L);
        produto1.setNome("Filtro de Óleo");

        ProdutoCatalogoResponseDTO produto2 = new ProdutoCatalogoResponseDTO();
        produto2.setId(2L);
        produto2.setNome("Pastilha de Freio");

        List<ProdutoCatalogoResponseDTO> produtos = Arrays.asList(produto1, produto2);

        when(produtoCatalogoService.listarTodos()).thenReturn(produtos);

        // Act & Assert
        mockMvc.perform(get("/api/catalogo-produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(produtoCatalogoService, times(1)).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar 204 quando lista vazia")
    void deveRetornar204QuandoListaVazia() throws Exception {
        // Arrange
        when(produtoCatalogoService.listarTodos()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/catalogo-produtos"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve buscar produto por ID")
    void deveBuscarProdutoPorId() throws Exception {
        // Arrange
        ProdutoCatalogoResponseDTO response = new ProdutoCatalogoResponseDTO();
        response.setId(1L);
        response.setNome("Filtro de Óleo");

        when(produtoCatalogoService.buscarPorId(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/catalogo-produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(produtoCatalogoService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar 404 quando produto não encontrado")
    void deveRetornar404QuandoProdutoNaoEncontrado() throws Exception {
        // Arrange
        when(produtoCatalogoService.buscarPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Produto não encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/catalogo-produtos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar produto")
    void deveAtualizarProduto() throws Exception {
        // Arrange
        ProdutoCatalogoRequestDTO request = new ProdutoCatalogoRequestDTO();
        request.setNome("Filtro de Óleo Atualizado");
        request.setCategoria(br.com.fiap.oficina.shared.enums.CategoriaProduto.PECA);
        request.setPreco(BigDecimal.valueOf(50.00));

        ProdutoCatalogoResponseDTO response = new ProdutoCatalogoResponseDTO();
        response.setId(1L);
        response.setNome("Filtro de Óleo Atualizado");

        when(produtoCatalogoService.atualizar(eq(1L), any(ProdutoCatalogoRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/catalogo-produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(produtoCatalogoService, times(1)).atualizar(eq(1L), any(ProdutoCatalogoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve deletar produto")
    void deveDeletarProduto() throws Exception {
        // Arrange
        doNothing().when(produtoCatalogoService).deletar(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/catalogo-produtos/1"))
                .andExpect(status().isNoContent());

        verify(produtoCatalogoService, times(1)).deletar(1L);
    }

    @Test
    @DisplayName("Deve buscar produtos por termo")
    void deveBuscarProdutosPorTermo() throws Exception {
        // Arrange
        ProdutoCatalogoResponseDTO produto = new ProdutoCatalogoResponseDTO();
        produto.setId(1L);
        produto.setNome("Filtro de Óleo");

        when(produtoCatalogoService.buscarPorTermo("filtro")).thenReturn(Arrays.asList(produto));

        // Act & Assert
        mockMvc.perform(get("/api/catalogo-produtos/buscar")
                        .param("termo", "filtro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(produtoCatalogoService, times(1)).buscarPorTermo("filtro");
    }
}
