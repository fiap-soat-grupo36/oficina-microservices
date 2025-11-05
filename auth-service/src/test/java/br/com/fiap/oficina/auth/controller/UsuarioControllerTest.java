package br.com.fiap.oficina.auth.controller;

import br.com.fiap.oficina.auth.dto.request.UsuarioRequestDTO;
import br.com.fiap.oficina.auth.dto.response.UsuarioResponseDTO;
import br.com.fiap.oficina.auth.service.UsuarioService;
import br.com.fiap.oficina.shared.enums.Role;
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

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso")
    void deveCadastrarUsuarioComSucesso() throws Exception {
        // Arrange
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setUsername("joao");
        request.setPassword("senha123");
        request.setNome("João Silva");
        request.setRole(Role.USER);

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setUsername("joao");
        response.setNome("João Silva");
        response.setRole("USER");

        when(usuarioService.cadastrar(any(UsuarioRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("joao"));

        verify(usuarioService, times(1)).cadastrar(any(UsuarioRequestDTO.class));
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void deveListarTodosOsUsuarios() throws Exception {
        // Arrange
        UsuarioResponseDTO user1 = new UsuarioResponseDTO();
        user1.setId(1L);
        user1.setUsername("joao");

        UsuarioResponseDTO user2 = new UsuarioResponseDTO();
        user2.setId(2L);
        user2.setUsername("maria");

        List<UsuarioResponseDTO> usuarios = Arrays.asList(user1, user2);

        when(usuarioService.listarTodos()).thenReturn(usuarios);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(usuarioService, times(1)).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar 204 quando lista vazia")
    void deveRetornar204QuandoListaVazia() throws Exception {
        // Arrange
        when(usuarioService.listarTodos()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void deveBuscarUsuarioPorId() throws Exception {
        // Arrange
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setUsername("joao");

        when(usuarioService.buscarPorId(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("joao"));

        verify(usuarioService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar 404 quando usuário não encontrado")
    void deveRetornar404QuandoUsuarioNaoEncontrado() throws Exception {
        // Arrange
        when(usuarioService.buscarPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Usuário não encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar usuário")
    void deveAtualizarUsuario() throws Exception {
        // Arrange
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setUsername("joao");
        request.setPassword("novaSenha123");
        request.setNome("João Silva Atualizado");
        request.setRole(Role.ADMIN);

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setUsername("joao");
        response.setNome("João Silva Atualizado");
        response.setRole("ADMIN");

        when(usuarioService.atualizar(eq(1L), any(UsuarioRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva Atualizado"));

        verify(usuarioService, times(1)).atualizar(eq(1L), any(UsuarioRequestDTO.class));
    }

    @Test
    @DisplayName("Deve deletar usuário")
    void deveDeletarUsuario() throws Exception {
        // Arrange
        doNothing().when(usuarioService).deletar(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).deletar(1L);
    }

    @Test
    @DisplayName("Deve buscar usuário por username")
    void deveBuscarUsuarioPorUsername() throws Exception {
        // Arrange
        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setUsername("joao");

        when(usuarioService.buscarPorUsernameComExcecao("joao")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/buscar")
                        .param("username", "joao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joao"));
    }

    @Test
    @DisplayName("Deve buscar usuários por role")
    void deveBuscarUsuariosPorRole() throws Exception {
        // Arrange
        UsuarioResponseDTO user1 = new UsuarioResponseDTO();
        user1.setId(1L);
        user1.setRole("ADMIN");

        List<UsuarioResponseDTO> usuarios = Arrays.asList(user1);

        when(usuarioService.buscarPorRole("ADMIN")).thenReturn(usuarios);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/buscar")
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
