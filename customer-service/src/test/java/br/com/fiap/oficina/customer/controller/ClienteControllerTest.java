package br.com.fiap.oficina.customer.controller;

import br.com.fiap.oficina.customer.dto.request.ClienteRequestDTO;
import br.com.fiap.oficina.customer.dto.response.ClienteResponseDTO;
import br.com.fiap.oficina.customer.service.ClienteService;
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

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso")
    void deveCadastrarClienteComSucesso() throws Exception {
        // Arrange
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setNome("João Silva");
        request.setCpf("12345678901");
        request.setEmail("joao@email.com");
        request.setTelefone("11999999999");

        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(1L);
        response.setNome("João Silva");
        response.setEmail("joao@email.com");

        when(clienteService.salvar(any(ClienteRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João Silva"));

        verify(clienteService, times(1)).salvar(any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando dados inválidos")
    void deveRetornarErro400QuandoDadosInvalidos() throws Exception {
        // Arrange
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setNome(""); // Nome vazio

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void deveListarTodosOsClientes() throws Exception {
        // Arrange
        ClienteResponseDTO cliente1 = new ClienteResponseDTO();
        cliente1.setId(1L);
        cliente1.setNome("João Silva");

        ClienteResponseDTO cliente2 = new ClienteResponseDTO();
        cliente2.setId(2L);
        cliente2.setNome("Maria Santos");

        List<ClienteResponseDTO> clientes = Arrays.asList(cliente1, cliente2);

        when(clienteService.listarClientes()).thenReturn(clientes);

        // Act & Assert
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("João Silva"))
                .andExpect(jsonPath("$[1].nome").value("Maria Santos"));

        verify(clienteService, times(1)).listarClientes();
    }

    @Test
    @DisplayName("Deve retornar 204 quando lista vazia")
    void deveRetornar204QuandoListaVazia() throws Exception {
        // Arrange
        when(clienteService.listarClientes()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isNoContent());

        verify(clienteService, times(1)).listarClientes();
    }

    @Test
    @DisplayName("Deve buscar cliente por ID")
    void deveBuscarClientePorId() throws Exception {
        // Arrange
        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(1L);
        response.setNome("João Silva");
        response.setEmail("joao@email.com");

        when(clienteService.buscarPorId(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João Silva"));

        verify(clienteService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar 404 quando cliente não encontrado")
    void deveRetornar404QuandoClienteNaoEncontrado() throws Exception {
        // Arrange
        when(clienteService.buscarPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/clientes/999"))
                .andExpect(status().isNotFound());

        verify(clienteService, times(1)).buscarPorId(999L);
    }

    @Test
    @DisplayName("Deve buscar cliente por CPF")
    void deveBuscarClientePorCpf() throws Exception {
        // Arrange
        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(1L);
        response.setNome("João Silva");
        response.setCpf("123.456.789-01");

        when(clienteService.buscarPorCpf("12345678901")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/clientes/buscar")
                        .param("cpf", "12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João Silva"));

        verify(clienteService, times(1)).buscarPorCpf("12345678901");
    }

    @Test
    @DisplayName("Deve buscar cliente por email")
    void deveBuscarClientePorEmail() throws Exception {
        // Arrange
        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(1L);
        response.setNome("João Silva");
        response.setEmail("joao@email.com");

        when(clienteService.buscarPorEmail("joao@email.com")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/clientes/buscar")
                        .param("email", "joao@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("joao@email.com"));

        verify(clienteService, times(1)).buscarPorEmail("joao@email.com");
    }

    @Test
    @DisplayName("Deve atualizar cliente")
    void deveAtualizarCliente() throws Exception {
        // Arrange
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setNome("João Silva Atualizado");
        request.setEmail("joao.novo@email.com");
        request.setTelefone("11988888888");

        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(1L);
        response.setNome("João Silva Atualizado");
        response.setEmail("joao.novo@email.com");

        when(clienteService.atualizar(eq(1L), any(ClienteRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João Silva Atualizado"));

        verify(clienteService, times(1)).atualizar(eq(1L), any(ClienteRequestDTO.class));
    }

    @Test
    @DisplayName("Deve deletar cliente")
    void deveDeletarCliente() throws Exception {
        // Arrange
        doNothing().when(clienteService).deletar(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());

        verify(clienteService, times(1)).deletar(1L);
    }
}
