package br.com.fiap.oficina.customer.controller;

import br.com.fiap.oficina.customer.dto.request.VeiculoRequesDTO;
import br.com.fiap.oficina.customer.dto.response.VeiculoResponseDTO;
import br.com.fiap.oficina.customer.service.VeiculoService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VeiculoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VeiculoService veiculoService;

    @Test
    @DisplayName("Deve criar veículo com sucesso")
    void deveCriarVeiculoComSucesso() throws Exception {
        // Arrange
        VeiculoRequesDTO request = new VeiculoRequesDTO();
        request.setPlaca("ABC1D23");
        request.setModelo("Civic");
        request.setMarca("Honda");
        request.setAno(2023);
        request.setClienteId(1L);

        VeiculoResponseDTO response = new VeiculoResponseDTO();
        response.setId(1L);
        response.setPlaca("ABC1D23");
        response.setModelo("Civic");

        when(veiculoService.salvar(any(VeiculoRequesDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.placa").value("ABC1D23"));

        verify(veiculoService, times(1)).salvar(any(VeiculoRequesDTO.class));
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando dados inválidos")
    void deveRetornarErro400QuandoDadosInvalidos() throws Exception {
        // Arrange
        VeiculoRequesDTO request = new VeiculoRequesDTO();
        request.setPlaca(""); // Placa vazia

        // Act & Assert
        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(veiculoService, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve listar todos os veículos")
    void deveListarTodosOsVeiculos() throws Exception {
        // Arrange
        VeiculoResponseDTO veiculo1 = new VeiculoResponseDTO();
        veiculo1.setId(1L);
        veiculo1.setPlaca("ABC1D23");

        VeiculoResponseDTO veiculo2 = new VeiculoResponseDTO();
        veiculo2.setId(2L);
        veiculo2.setPlaca("XYZ9W87");

        List<VeiculoResponseDTO> veiculos = Arrays.asList(veiculo1, veiculo2);

        when(veiculoService.listarTodos()).thenReturn(veiculos);

        // Act & Assert
        mockMvc.perform(get("/api/veiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].placa").value("ABC1D23"))
                .andExpect(jsonPath("$[1].placa").value("XYZ9W87"));

        verify(veiculoService, times(1)).listarTodos();
    }

    @Test
    @DisplayName("Deve buscar veículo por ID")
    void deveBuscarVeiculoPorId() throws Exception {
        // Arrange
        VeiculoResponseDTO response = new VeiculoResponseDTO();
        response.setId(1L);
        response.setPlaca("ABC1D23");
        response.setModelo("Civic");

        when(veiculoService.buscarPorId(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/veiculos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.placa").value("ABC1D23"));

        verify(veiculoService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar 404 quando veículo não encontrado")
    void deveRetornar404QuandoVeiculoNaoEncontrado() throws Exception {
        // Arrange
        when(veiculoService.buscarPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Veículo não encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/veiculos/999"))
                .andExpect(status().isNotFound());

        verify(veiculoService, times(1)).buscarPorId(999L);
    }

    @Test
    @DisplayName("Deve buscar veículo por placa")
    void deveBuscarVeiculoPorPlaca() throws Exception {
        // Arrange
        VeiculoResponseDTO response = new VeiculoResponseDTO();
        response.setId(1L);
        response.setPlaca("ABC1D23");

        when(veiculoService.buscarPorPlaca("ABC1D23")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/veiculos/placa/ABC1D23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.placa").value("ABC1D23"));

        verify(veiculoService, times(1)).buscarPorPlaca("ABC1D23");
    }

    @Test
    @DisplayName("Deve buscar veículos por cliente")
    void deveBuscarVeiculosPorCliente() throws Exception {
        // Arrange
        VeiculoResponseDTO veiculo1 = new VeiculoResponseDTO();
        veiculo1.setId(1L);
        veiculo1.setPlaca("ABC1D23");
        veiculo1.setClienteId(1L);

        List<VeiculoResponseDTO> veiculos = Arrays.asList(veiculo1);

        when(veiculoService.buscarPorCliente(1L)).thenReturn(veiculos);

        // Act & Assert
        mockMvc.perform(get("/api/veiculos/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(1));

        verify(veiculoService, times(1)).buscarPorCliente(1L);
    }

    @Test
    @DisplayName("Deve retornar 204 quando cliente não tem veículos")
    void deveRetornar204QuandoClienteSemVeiculos() throws Exception {
        // Arrange
        when(veiculoService.buscarPorCliente(1L)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/veiculos/cliente/1"))
                .andExpect(status().isNoContent());

        verify(veiculoService, times(1)).buscarPorCliente(1L);
    }

    @Test
    @DisplayName("Deve atualizar veículo")
    void deveAtualizarVeiculo() throws Exception {
        // Arrange
        VeiculoRequesDTO request = new VeiculoRequesDTO();
        request.setPlaca("ABC1D23");
        request.setModelo("Civic 2024");
        request.setAno(2024);
        request.setClienteId(1L);

        VeiculoResponseDTO response = new VeiculoResponseDTO();
        response.setId(1L);
        response.setPlaca("ABC1D23");
        response.setModelo("Civic 2024");

        when(veiculoService.atualizar(eq(1L), any(VeiculoRequesDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.modelo").value("Civic 2024"));

        verify(veiculoService, times(1)).atualizar(eq(1L), any(VeiculoRequesDTO.class));
    }

    @Test
    @DisplayName("Deve transferir veículo")
    void deveTransferirVeiculo() throws Exception {
        // Arrange
        VeiculoResponseDTO response = new VeiculoResponseDTO();
        response.setId(1L);
        response.setPlaca("ABC1D23");
        response.setClienteId(2L);

        when(veiculoService.transferirPropriedade(1L, 2L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/veiculos/1/transferir")
                        .param("novoClienteId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteId").value(2));

        verify(veiculoService, times(1)).transferirPropriedade(1L, 2L);
    }

    @Test
    @DisplayName("Deve deletar veículo")
    void deveDeletarVeiculo() throws Exception {
        // Arrange
        doNothing().when(veiculoService).deletar(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/veiculos/1"))
                .andExpect(status().isNoContent());

        verify(veiculoService, times(1)).deletar(1L);
    }
}