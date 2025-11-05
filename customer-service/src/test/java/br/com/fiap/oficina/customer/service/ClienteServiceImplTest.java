package br.com.fiap.oficina.customer.service;

import br.com.fiap.oficina.customer.dto.request.ClienteRequestDTO;
import br.com.fiap.oficina.customer.dto.response.ClienteResponseDTO;
import br.com.fiap.oficina.customer.entity.Cliente;
import br.com.fiap.oficina.customer.mapper.ClienteMapper;
import br.com.fiap.oficina.customer.repository.ClienteRepository;
import br.com.fiap.oficina.customer.service.impl.ClienteServiceImpl;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    @Test
    @DisplayName("Deve salvar cliente com CPF")
    void deveSalvarClienteComCpf() {
        // Arrange
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setNome("João Silva");
        request.setCpf("12345678901");
        request.setEmail("joao@email.com");
        request.setTelefone("11999999999");

        Cliente entity = new Cliente();
        entity.setNome("João Silva");
        entity.setCpf("12345678901");
        entity.setEmail("joao@email.com");

        Cliente savedEntity = new Cliente();
        savedEntity.setId(1L);
        savedEntity.setNome("João Silva");
        savedEntity.setCpf("123.456.789-01");
        savedEntity.setEmail("joao@email.com");

        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(1L);
        response.setNome("João Silva");

        when(clienteMapper.toEntity(request)).thenReturn(entity);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(savedEntity);
        when(clienteMapper.toDTO(savedEntity)).thenReturn(response);

        // Act
        ClienteResponseDTO result = clienteService.salvar(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("João Silva", result.getNome());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve buscar cliente por ID")
    void deveBuscarClientePorId() {
        // Arrange
        Long id = 1L;
        Cliente entity = new Cliente();
        entity.setId(id);
        entity.setNome("João Silva");

        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(id);
        response.setNome("João Silva");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));
        when(clienteMapper.toDTO(entity)).thenReturn(response);

        // Act
        ClienteResponseDTO result = clienteService.buscarPorId(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("João Silva", result.getNome());
        verify(clienteRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não encontrado por ID")
    void deveLancarExcecaoQuandoClienteNaoEncontradoPorId() {
        // Arrange
        Long id = 999L;
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.buscarPorId(id));
        verify(clienteRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void deveListarTodosOsClientes() {
        // Arrange
        Cliente cliente1 = new Cliente();
        cliente1.setId(1L);
        cliente1.setNome("João Silva");

        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);
        cliente2.setNome("Maria Santos");

        List<Cliente> clientes = Arrays.asList(cliente1, cliente2);

        ClienteResponseDTO response1 = new ClienteResponseDTO();
        response1.setId(1L);
        response1.setNome("João Silva");

        ClienteResponseDTO response2 = new ClienteResponseDTO();
        response2.setId(2L);
        response2.setNome("Maria Santos");

        List<ClienteResponseDTO> responses = Arrays.asList(response1, response2);

        when(clienteRepository.findAll()).thenReturn(clientes);
        when(clienteMapper.toDTO(clientes)).thenReturn(responses);

        // Act
        List<ClienteResponseDTO> result = clienteService.listarClientes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("João Silva", result.get(0).getNome());
        assertEquals("Maria Santos", result.get(1).getNome());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar cliente por CPF")
    void deveBuscarClientePorCpf() {
        // Arrange
        String cpf = "11144477735"; // CPF válido para testes
        String cpfFormatado = "111.444.777-35";
        
        Cliente entity = new Cliente();
        entity.setId(1L);
        entity.setCpf(cpfFormatado);
        entity.setNome("João Silva");

        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(1L);
        response.setCpf(cpfFormatado);

        when(clienteRepository.findByCpf(cpfFormatado)).thenReturn(Optional.of(entity));
        when(clienteMapper.toDTO(entity)).thenReturn(response);

        // Act
        ClienteResponseDTO result = clienteService.buscarPorCpf(cpf);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(clienteRepository, times(1)).findByCpf(cpfFormatado);
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF inválido")
    void deveLancarExcecaoQuandoCpfInvalido() {
        // Arrange
        String cpfInvalido = "00000000000";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> clienteService.buscarPorCpf(cpfInvalido));
        verify(clienteRepository, never()).findByCpf(any());
    }

    @Test
    @DisplayName("Deve buscar cliente por email")
    void deveBuscarClientePorEmail() {
        // Arrange
        String email = "joao@email.com";
        
        Cliente entity = new Cliente();
        entity.setId(1L);
        entity.setEmail(email);
        entity.setNome("João Silva");

        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(1L);
        response.setEmail(email);

        when(clienteRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(entity));
        when(clienteMapper.toDTO(entity)).thenReturn(response);

        // Act
        ClienteResponseDTO result = clienteService.buscarPorEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(email, result.getEmail());
        verify(clienteRepository, times(1)).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("Deve lançar exceção quando email vazio")
    void deveLancarExcecaoQuandoEmailVazio() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> clienteService.buscarPorEmail(""));
        verify(clienteRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    @DisplayName("Deve atualizar cliente")
    void deveAtualizarCliente() {
        // Arrange
        Long id = 1L;
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setNome("João Silva Atualizado");
        request.setEmail("joao.novo@email.com");
        request.setTelefone("11988888888");

        Cliente existingEntity = new Cliente();
        existingEntity.setId(id);
        existingEntity.setNome("João Silva");
        existingEntity.setEmail("joao@email.com");

        Cliente updatedEntity = new Cliente();
        updatedEntity.setId(id);
        updatedEntity.setNome("João Silva Atualizado");
        updatedEntity.setEmail("joao.novo@email.com");

        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(id);
        response.setNome("João Silva Atualizado");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(updatedEntity);
        when(clienteMapper.toDTO(updatedEntity)).thenReturn(response);

        // Act
        ClienteResponseDTO result = clienteService.atualizar(id, request);

        // Assert
        assertNotNull(result);
        assertEquals("João Silva Atualizado", result.getNome());
        verify(clienteRepository, times(1)).findById(id);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve deletar cliente (soft delete)")
    void deveDeletarCliente() {
        // Arrange
        Long id = 1L;
        Cliente entity = new Cliente();
        entity.setId(id);
        entity.setAtivo(true);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(entity);

        // Act
        clienteService.deletar(id);

        // Assert
        verify(clienteRepository, times(1)).findById(id);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve buscar clientes por nome")
    void deveBuscarClientesPorNome() {
        // Arrange
        String nome = "João";
        Cliente cliente1 = new Cliente();
        cliente1.setId(1L);
        cliente1.setNome("João Silva");

        List<Cliente> clientes = Arrays.asList(cliente1);
        
        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(1L);
        response.setNome("João Silva");

        when(clienteRepository.findByNomeContainingIgnoreCase(nome.toLowerCase())).thenReturn(clientes);
        when(clienteMapper.toDTO(cliente1)).thenReturn(response);

        // Act
        List<ClienteResponseDTO> result = clienteService.buscarPorNome(nome);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(clienteRepository, times(1)).findByNomeContainingIgnoreCase(nome.toLowerCase());
    }
}
