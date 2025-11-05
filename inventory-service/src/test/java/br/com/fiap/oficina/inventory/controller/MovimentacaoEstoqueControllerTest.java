package br.com.fiap.oficina.inventory.controller;

import br.com.fiap.oficina.inventory.dto.request.MovimentacaoEstoqueRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.MovimentacaoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.service.MovimentacaoEstoqueService;
import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovimentacaoEstoqueController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MovimentacaoEstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @Test
    @DisplayName("Deve registrar entrada de estoque")
    void deveRegistrarEntradaDeEstoque() throws Exception {
        // Arrange
        MovimentacaoEstoqueRequestDTO request = new MovimentacaoEstoqueRequestDTO();
        request.setProdutoCatalogoId(100L);
        request.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        request.setQuantidade(50);
        request.setPrecoUnitario(BigDecimal.valueOf(10.00));

        MovimentacaoEstoqueResponseDTO response = new MovimentacaoEstoqueResponseDTO();
        response.setId(1L);
        response.setProdutoCatalogoId(100L);
        response.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        response.setQuantidade(50);
        response.setPrecoUnitario(BigDecimal.valueOf(10.00));

        when(movimentacaoEstoqueService.registrarEntrada(any(MovimentacaoEstoqueRequestDTO.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/estoque/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipoMovimentacao").value("ENTRADA"))
                .andExpect(jsonPath("$.quantidade").value(50));

        verify(movimentacaoEstoqueService, times(1)).registrarEntrada(any(MovimentacaoEstoqueRequestDTO.class));
    }

    @Test
    @DisplayName("Deve registrar saída de estoque")
    void deveRegistrarSaidaDeEstoque() throws Exception {
        // Arrange
        MovimentacaoEstoqueRequestDTO request = new MovimentacaoEstoqueRequestDTO();
        request.setProdutoCatalogoId(100L);
        request.setTipoMovimentacao(TipoMovimentacao.SAIDA);
        request.setQuantidade(20);

        MovimentacaoEstoqueResponseDTO response = new MovimentacaoEstoqueResponseDTO();
        response.setId(2L);
        response.setProdutoCatalogoId(100L);
        response.setTipoMovimentacao(TipoMovimentacao.SAIDA);
        response.setQuantidade(20);

        when(movimentacaoEstoqueService.registrarSaida(any(MovimentacaoEstoqueRequestDTO.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/estoque/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.tipoMovimentacao").value("SAIDA"));

        verify(movimentacaoEstoqueService, times(1)).registrarSaida(any(MovimentacaoEstoqueRequestDTO.class));
    }

    @Test
    @DisplayName("Deve listar movimentações sem filtros")
    void deveListarMovimentacoesSemFiltros() throws Exception {
        // Arrange
        MovimentacaoEstoqueResponseDTO mov1 = new MovimentacaoEstoqueResponseDTO();
        mov1.setId(1L);
        mov1.setTipoMovimentacao(TipoMovimentacao.ENTRADA);

        MovimentacaoEstoqueResponseDTO mov2 = new MovimentacaoEstoqueResponseDTO();
        mov2.setId(2L);
        mov2.setTipoMovimentacao(TipoMovimentacao.SAIDA);

        List<MovimentacaoEstoqueResponseDTO> movimentacoes = Arrays.asList(mov1, mov2);

        when(movimentacaoEstoqueService.listarMovimentacoes(null, null, null, null))
                .thenReturn(movimentacoes);

        // Act & Assert
        mockMvc.perform(get("/estoque/movimentacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(movimentacaoEstoqueService, times(1)).listarMovimentacoes(null, null, null, null);
    }

    @Test
    @DisplayName("Deve listar movimentações filtradas por produto")
    void deveListarMovimentacoesFiltradasPorProduto() throws Exception {
        // Arrange
        MovimentacaoEstoqueResponseDTO mov = new MovimentacaoEstoqueResponseDTO();
        mov.setId(1L);
        mov.setProdutoCatalogoId(100L);

        when(movimentacaoEstoqueService.listarMovimentacoes(eq(100L), any(), any(), any()))
                .thenReturn(Arrays.asList(mov));

        // Act & Assert
        mockMvc.perform(get("/estoque/movimentacoes").param("produtoCatalogoId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].produtoCatalogoId").value(100));

        verify(movimentacaoEstoqueService, times(1)).listarMovimentacoes(eq(100L), any(), any(), any());
    }

    @Test
    @DisplayName("Deve buscar movimentação por ID")
    void deveBuscarMovimentacaoPorId() throws Exception {
        // Arrange
        MovimentacaoEstoqueResponseDTO mov = new MovimentacaoEstoqueResponseDTO();
        mov.setId(1L);
        mov.setProdutoCatalogoId(100L);
        mov.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        mov.setQuantidade(50);

        when(movimentacaoEstoqueService.buscarPorId(1L)).thenReturn(mov);

        // Act & Assert
        mockMvc.perform(get("/estoque/movimentacoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.produtoCatalogoId").value(100))
                .andExpect(jsonPath("$.quantidade").value(50));

        verify(movimentacaoEstoqueService, times(1)).buscarPorId(1L);
    }
}
