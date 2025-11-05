package br.com.fiap.oficina.inventory.controller;

import br.com.fiap.oficina.inventory.dto.request.ItemReservaDTO;
import br.com.fiap.oficina.inventory.dto.request.ReservaLoteRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.ItemReservaResultDTO;
import br.com.fiap.oficina.inventory.dto.response.ReservaLoteResponseDTO;
import br.com.fiap.oficina.inventory.service.ReservaEstoqueLoteService;
import br.com.fiap.oficina.inventory.service.ReservaEstoqueService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaEstoqueController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ReservaEstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservaEstoqueLoteService reservaEstoqueLoteService;

    @MockBean
    private ReservaEstoqueService reservaEstoqueService;

    @Test
    @DisplayName("Deve reservar em lote com sucesso")
    void deveReservarEmLoteComSucesso() throws Exception {
        // Arrange
        ItemReservaDTO item1 = new ItemReservaDTO();
        item1.setProdutoCatalogoId(100L);
        item1.setQuantidade(10);

        ItemReservaDTO item2 = new ItemReservaDTO();
        item2.setProdutoCatalogoId(200L);
        item2.setQuantidade(5);

        ReservaLoteRequestDTO request = new ReservaLoteRequestDTO();
        request.setOrdemServicoId(1L);
        request.setItens(Arrays.asList(item1, item2));

        ItemReservaResultDTO result1 = new ItemReservaResultDTO();
        result1.setProdutoCatalogoId(100L);
        result1.setQuantidadeReservada(10);
        result1.setSucesso(true);

        ItemReservaResultDTO result2 = new ItemReservaResultDTO();
        result2.setProdutoCatalogoId(200L);
        result2.setQuantidadeReservada(5);
        result2.setSucesso(true);

        ReservaLoteResponseDTO response = new ReservaLoteResponseDTO();
        response.setOrdemServicoId(1L);
        response.setResultados(Arrays.asList(result1, result2));

        when(reservaEstoqueLoteService.reservarEmLote(any(ReservaLoteRequestDTO.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/reservas/lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ordemServicoId").value(1))
                .andExpect(jsonPath("$.resultados").isArray())
                .andExpect(jsonPath("$.resultados.length()").value(2));

        verify(reservaEstoqueLoteService, times(1)).reservarEmLote(any(ReservaLoteRequestDTO.class));
    }

    @Test
    @DisplayName("Deve cancelar reservas por ordem de serviço")
    void deveCancelarReservasPorOrdemServico() throws Exception {
        // Arrange
        doNothing().when(reservaEstoqueService).cancelarPorOrdemServico(1L);

        // Act & Assert
        mockMvc.perform(delete("/reservas/os/1"))
                .andExpect(status().isNoContent());

        verify(reservaEstoqueService, times(1)).cancelarPorOrdemServico(1L);
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando dados inválidos")
    void deveRetornarErro400QuandoDadosInvalidos() throws Exception {
        // Arrange
        ReservaLoteRequestDTO request = new ReservaLoteRequestDTO();
        // Sem ordemServicoId e sem itens

        // Act & Assert
        mockMvc.perform(post("/reservas/lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(reservaEstoqueLoteService, never()).reservarEmLote(any());
    }
}
