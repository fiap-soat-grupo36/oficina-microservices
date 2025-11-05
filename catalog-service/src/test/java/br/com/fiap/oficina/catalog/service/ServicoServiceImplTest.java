package br.com.fiap.oficina.catalog.service;

import br.com.fiap.oficina.catalog.dto.request.ServicoRequestDTO;
import br.com.fiap.oficina.catalog.dto.response.ServicoResponseDTO;
import br.com.fiap.oficina.catalog.entity.Servico;
import br.com.fiap.oficina.catalog.mapper.ServicoMapper;
import br.com.fiap.oficina.catalog.repository.ServicoRepository;
import br.com.fiap.oficina.catalog.service.impl.ServicoServiceImpl;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceImplTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ServicoMapper servicoMapper;

    @InjectMocks
    private ServicoServiceImpl servicoService;

    @Test
    @DisplayName("Deve salvar serviço com sucesso")
    void deveSalvarServicoComSucesso() {
        ServicoRequestDTO request = new ServicoRequestDTO();
        request.setNome("Troca de óleo");
        request.setPrecoBase(BigDecimal.valueOf(100.00));

        Servico entity = new Servico();
        entity.setNome("Troca de óleo");

        Servico savedEntity = new Servico();
        savedEntity.setId(1L);
        savedEntity.setNome("Troca de óleo");

        ServicoResponseDTO response = new ServicoResponseDTO();
        response.setId(1L);
        response.setNome("Troca de óleo");

        when(servicoMapper.toEntity(request)).thenReturn(entity);
        when(servicoRepository.save(any(Servico.class))).thenReturn(savedEntity);
        when(servicoMapper.toDTO(savedEntity)).thenReturn(response);

        ServicoResponseDTO result = servicoService.salvar(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Troca de óleo", result.getNome());
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve buscar serviço por ID")
    void deveBuscarServicoPorId() {
        Long id = 1L;
        Servico entity = new Servico();
        entity.setId(id);
        entity.setNome("Troca de óleo");

        ServicoResponseDTO response = new ServicoResponseDTO();
        response.setId(id);
        response.setNome("Troca de óleo");

        when(servicoRepository.findById(id)).thenReturn(Optional.of(entity));
        when(servicoMapper.toDTO(entity)).thenReturn(response);

        ServicoResponseDTO result = servicoService.buscarPorId(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(servicoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando serviço não encontrado")
    void deveLancarExcecaoQuandoServicoNaoEncontrado() {
        Long id = 999L;
        when(servicoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servicoService.buscarPorId(id));
        verify(servicoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve listar todos os serviços")
    void deveListarTodosOsServicos() {
        Servico servico1 = new Servico();
        servico1.setId(1L);
        servico1.setNome("Troca de óleo");

        Servico servico2 = new Servico();
        servico2.setId(2L);
        servico2.setNome("Alinhamento");

        List<Servico> servicos = Arrays.asList(servico1, servico2);

        ServicoResponseDTO response1 = new ServicoResponseDTO();
        response1.setId(1L);

        ServicoResponseDTO response2 = new ServicoResponseDTO();
        response2.setId(2L);

        List<ServicoResponseDTO> responses = Arrays.asList(response1, response2);

        when(servicoRepository.findAll()).thenReturn(servicos);
        when(servicoMapper.toDTO(servicos)).thenReturn(responses);

        List<ServicoResponseDTO> result = servicoService.listarTodos();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(servicoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve atualizar serviço")
    void deveAtualizarServico() {
        Long id = 1L;
        ServicoRequestDTO request = new ServicoRequestDTO();
        request.setNome("Troca de óleo atualizada");

        Servico existingEntity = new Servico();
        existingEntity.setId(id);
        existingEntity.setNome("Troca de óleo");

        Servico updatedEntity = new Servico();
        updatedEntity.setId(id);
        updatedEntity.setNome("Troca de óleo atualizada");

        ServicoResponseDTO response = new ServicoResponseDTO();
        response.setId(id);
        response.setNome("Troca de óleo atualizada");

        when(servicoRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(servicoRepository.save(any(Servico.class))).thenReturn(updatedEntity);
        when(servicoMapper.toDTO(updatedEntity)).thenReturn(response);

        ServicoResponseDTO result = servicoService.atualizar(id, request);

        assertNotNull(result);
        assertEquals("Troca de óleo atualizada", result.getNome());
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve deletar serviço")
    void deveDeletarServico() {
        Long id = 1L;
        Servico entity = new Servico();
        entity.setId(id);

        when(servicoRepository.findById(id)).thenReturn(Optional.of(entity));
        when(servicoRepository.save(any(Servico.class))).thenReturn(entity);

        servicoService.deletar(id);

        verify(servicoRepository, times(1)).findById(id);
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }
}
