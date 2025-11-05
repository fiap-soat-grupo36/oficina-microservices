package br.com.fiap.oficina.catalog.controller;

import br.com.fiap.oficina.catalog.dto.request.ServicoRequestDTO;
import br.com.fiap.oficina.catalog.dto.response.ServicoResponseDTO;
import br.com.fiap.oficina.catalog.service.ServicoService;
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

@WebMvcTest(ServicoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ServicoService servicoService;

    @Test
    @DisplayName("Deve cadastrar serviço com sucesso")
    void deveCadastrarServicoComSucesso() throws Exception {
        ServicoRequestDTO request = new ServicoRequestDTO();
        request.setNome("Troca de óleo");
        request.setDescricao("Troca completa de óleo");
        request.setPrecoBase(BigDecimal.valueOf(100.00));
        request.setCategoria(br.com.fiap.oficina.shared.enums.CategoriaServico.MANUTENCAO_PREVENTIVA);

        ServicoResponseDTO response = new ServicoResponseDTO();
        response.setId(1L);
        response.setNome("Troca de óleo");

        when(servicoService.salvar(any(ServicoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(servicoService, times(1)).salvar(any(ServicoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve listar todos os serviços")
    void deveListarTodosOsServicos() throws Exception {
        ServicoResponseDTO servico1 = new ServicoResponseDTO();
        servico1.setId(1L);
        servico1.setNome("Troca de óleo");

        ServicoResponseDTO servico2 = new ServicoResponseDTO();
        servico2.setId(2L);
        servico2.setNome("Alinhamento");

        List<ServicoResponseDTO> servicos = Arrays.asList(servico1, servico2);

        when(servicoService.listarTodos()).thenReturn(servicos);

        mockMvc.perform(get("/api/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(servicoService, times(1)).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar 204 quando lista vazia")
    void deveRetornar204QuandoListaVazia() throws Exception {
        when(servicoService.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/servicos"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve buscar serviço por ID")
    void deveBuscarServicoPorId() throws Exception {
        ServicoResponseDTO response = new ServicoResponseDTO();
        response.setId(1L);
        response.setNome("Troca de óleo");

        when(servicoService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/api/servicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(servicoService, times(1)).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar 404 quando serviço não encontrado")
    void deveRetornar404QuandoServicoNaoEncontrado() throws Exception {
        when(servicoService.buscarPorId(999L))
                .thenThrow(new RecursoNaoEncontradoException("Serviço não encontrado"));

        mockMvc.perform(get("/api/servicos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar serviço")
    void deveAtualizarServico() throws Exception {
        ServicoRequestDTO request = new ServicoRequestDTO();
        request.setNome("Troca de óleo atualizada");
        request.setPrecoBase(BigDecimal.valueOf(120.00));
        request.setCategoria(br.com.fiap.oficina.shared.enums.CategoriaServico.MANUTENCAO_PREVENTIVA);

        ServicoResponseDTO response = new ServicoResponseDTO();
        response.setId(1L);
        response.setNome("Troca de óleo atualizada");

        when(servicoService.atualizar(eq(1L), any(ServicoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/servicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(servicoService, times(1)).atualizar(eq(1L), any(ServicoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve deletar serviço")
    void deveDeletarServico() throws Exception {
        doNothing().when(servicoService).deletar(1L);

        mockMvc.perform(delete("/api/servicos/1"))
                .andExpect(status().isNoContent());

        verify(servicoService, times(1)).deletar(1L);
    }
}

