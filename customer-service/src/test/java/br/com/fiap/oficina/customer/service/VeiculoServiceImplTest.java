package br.com.fiap.oficina.customer.service;

import br.com.fiap.oficina.customer.dto.request.VeiculoRequesDTO;
import br.com.fiap.oficina.customer.dto.response.VeiculoResponseDTO;
import br.com.fiap.oficina.customer.entity.Cliente;
import br.com.fiap.oficina.customer.entity.Veiculo;
import br.com.fiap.oficina.customer.mapper.VeiculoMapper;
import br.com.fiap.oficina.customer.repository.VeiculoRepository;
import br.com.fiap.oficina.customer.service.impl.VeiculoServiceImpl;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceImplTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private VeiculoMapper veiculoMapper;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private VeiculoServiceImpl veiculoService;

    @Test
    @DisplayName("Deve salvar veículo com sucesso")
    void deveSalvarVeiculoComSucesso() {
        // Arrange
        VeiculoRequesDTO request = new VeiculoRequesDTO();
        request.setPlaca("ABC1D23");
        request.setModelo("Civic");
        request.setMarca("Honda");
        request.setAno(2023);
        request.setClienteId(1L);

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");

        Veiculo entity = new Veiculo();
        entity.setPlaca("ABC1D23");
        entity.setModelo("Civic");

        Veiculo savedEntity = new Veiculo();
        savedEntity.setId(1L);
        savedEntity.setPlaca("ABC1D23");
        savedEntity.setModelo("Civic");
        savedEntity.setCliente(cliente);

        VeiculoResponseDTO response = new VeiculoResponseDTO();
        response.setId(1L);
        response.setPlaca("ABC1D23");
        response.setClienteId(1L);

        when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.empty());
        when(veiculoMapper.toEntity(request)).thenReturn(entity);
        when(clienteService.getCliente(1L)).thenReturn(cliente);
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(savedEntity);
        when(veiculoMapper.toDTO(savedEntity)).thenReturn(response);

        // Act
        VeiculoResponseDTO result = veiculoService.salvar(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ABC1D23", result.getPlaca());
        verify(veiculoRepository, times(1)).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando placa já existe")
    void deveLancarExcecaoQuandoPlacaJaExiste() {
        // Arrange
        VeiculoRequesDTO request = new VeiculoRequesDTO();
        request.setPlaca("ABC1D23");
        request.setModelo("Civic");
        request.setAno(2023);
        request.setClienteId(1L);

        Veiculo existingVeiculo = new Veiculo();
        existingVeiculo.setId(99L);
        existingVeiculo.setPlaca("ABC1D23");

        when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(existingVeiculo));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> veiculoService.salvar(request));
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todos os veículos")
    void deveListarTodosOsVeiculos() {
        // Arrange
        Veiculo veiculo1 = new Veiculo();
        veiculo1.setId(1L);
        veiculo1.setPlaca("ABC1D23");

        Veiculo veiculo2 = new Veiculo();
        veiculo2.setId(2L);
        veiculo2.setPlaca("XYZ9W87");

        List<Veiculo> veiculos = Arrays.asList(veiculo1, veiculo2);

        VeiculoResponseDTO response1 = new VeiculoResponseDTO();
        response1.setId(1L);
        response1.setPlaca("ABC1D23");

        VeiculoResponseDTO response2 = new VeiculoResponseDTO();
        response2.setId(2L);
        response2.setPlaca("XYZ9W87");

        List<VeiculoResponseDTO> responses = Arrays.asList(response1, response2);

        when(veiculoRepository.findAll()).thenReturn(veiculos);
        when(veiculoMapper.toDTOList(veiculos)).thenReturn(responses);

        // Act
        List<VeiculoResponseDTO> result = veiculoService.listarTodos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ABC1D23", result.get(0).getPlaca());
        verify(veiculoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar veículo por ID")
    void deveBuscarVeiculoPorId() {
        // Arrange
        Long id = 1L;
        Veiculo entity = new Veiculo();
        entity.setId(id);
        entity.setPlaca("ABC1D23");

        VeiculoResponseDTO response = new VeiculoResponseDTO();
        response.setId(id);
        response.setPlaca("ABC1D23");

        when(veiculoRepository.findById(id)).thenReturn(Optional.of(entity));
        when(veiculoMapper.toDTO(entity)).thenReturn(response);

        // Act
        VeiculoResponseDTO result = veiculoService.buscarPorId(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(veiculoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando veículo não encontrado por ID")
    void deveLancarExcecaoQuandoVeiculoNaoEncontradoPorId() {
        // Arrange
        Long id = 999L;
        when(veiculoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> veiculoService.buscarPorId(id));
        verify(veiculoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve buscar veículo por placa")
    void deveBuscarVeiculoPorPlaca() {
        // Arrange
        String placa = "ABC1D23";
        Veiculo entity = new Veiculo();
        entity.setId(1L);
        entity.setPlaca(placa);

        VeiculoResponseDTO response = new VeiculoResponseDTO();
        response.setId(1L);
        response.setPlaca(placa);

        when(veiculoRepository.findByPlaca(placa)).thenReturn(Optional.of(entity));
        when(veiculoMapper.toDTO(entity)).thenReturn(response);

        // Act
        VeiculoResponseDTO result = veiculoService.buscarPorPlaca(placa);

        // Assert
        assertNotNull(result);
        assertEquals(placa, result.getPlaca());
        verify(veiculoRepository, times(1)).findByPlaca(placa);
    }

    @Test
    @DisplayName("Deve lançar exceção quando veículo não encontrado por placa")
    void deveLancarExcecaoQuandoVeiculoNaoEncontradoPorPlaca() {
        // Arrange
        String placa = "XYZ9999";
        when(veiculoRepository.findByPlaca(placa)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> veiculoService.buscarPorPlaca(placa));
        verify(veiculoRepository, times(1)).findByPlaca(placa);
    }

    @Test
    @DisplayName("Deve buscar veículos por cliente")
    void deveBuscarVeiculosPorCliente() {
        // Arrange
        Long clienteId = 1L;
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);

        Veiculo veiculo1 = new Veiculo();
        veiculo1.setId(1L);
        veiculo1.setPlaca("ABC1D23");
        veiculo1.setCliente(cliente);

        List<Veiculo> veiculos = Arrays.asList(veiculo1);

        VeiculoResponseDTO response = new VeiculoResponseDTO();
        response.setId(1L);
        response.setPlaca("ABC1D23");
        response.setClienteId(clienteId);

        when(clienteService.getCliente(clienteId)).thenReturn(cliente);
        when(veiculoRepository.findByClienteId(clienteId)).thenReturn(veiculos);
        when(veiculoMapper.toDTOList(veiculos)).thenReturn(Arrays.asList(response));

        // Act
        List<VeiculoResponseDTO> result = veiculoService.buscarPorCliente(clienteId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(clienteId, result.get(0).getClienteId());
        verify(clienteService, times(1)).getCliente(clienteId);
        verify(veiculoRepository, times(1)).findByClienteId(clienteId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando cliente não tem veículos")
    void deveRetornarListaVaziaQuandoClienteSemVeiculos() {
        // Arrange
        Long clienteId = 1L;
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);

        when(clienteService.getCliente(clienteId)).thenReturn(cliente);
        when(veiculoRepository.findByClienteId(clienteId)).thenReturn(Collections.emptyList());
        when(veiculoMapper.toDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<VeiculoResponseDTO> result = veiculoService.buscarPorCliente(clienteId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve atualizar veículo")
    void deveAtualizarVeiculo() {
        // Arrange
        Long id = 1L;
        VeiculoRequesDTO request = new VeiculoRequesDTO();
        request.setPlaca("ABC1D23");
        request.setModelo("Civic 2024");
        request.setMarca("Honda");
        request.setAno(2024);
        request.setClienteId(1L);

        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Veiculo existingEntity = new Veiculo();
        existingEntity.setId(id);
        existingEntity.setPlaca("ABC1D23");
        existingEntity.setModelo("Civic");
        existingEntity.setCliente(cliente);

        Veiculo updatedEntity = new Veiculo();
        updatedEntity.setId(id);
        updatedEntity.setPlaca("ABC1D23");
        updatedEntity.setModelo("Civic 2024");
        updatedEntity.setCliente(cliente);

        VeiculoResponseDTO response = new VeiculoResponseDTO();
        response.setId(id);
        response.setModelo("Civic 2024");

        when(veiculoRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(updatedEntity);
        when(veiculoMapper.toDTO(updatedEntity)).thenReturn(response);

        // Act
        VeiculoResponseDTO result = veiculoService.atualizar(id, request);

        // Assert
        assertNotNull(result);
        assertEquals("Civic 2024", result.getModelo());
        verify(veiculoRepository, times(1)).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar com placa já existente")
    void deveLancarExcecaoAoAtualizarComPlacaExistente() {
        // Arrange
        Long id = 1L;
        VeiculoRequesDTO request = new VeiculoRequesDTO();
        request.setPlaca("XYZ9W87");
        request.setModelo("Civic");
        request.setAno(2023);
        request.setClienteId(1L);

        Veiculo existingEntity = new Veiculo();
        existingEntity.setId(id);
        existingEntity.setPlaca("ABC1D23");

        Veiculo veiculoComPlacaDuplicada = new Veiculo();
        veiculoComPlacaDuplicada.setId(99L);
        veiculoComPlacaDuplicada.setPlaca("XYZ9W87");

        when(veiculoRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(veiculoRepository.findByPlaca("XYZ9W87")).thenReturn(Optional.of(veiculoComPlacaDuplicada));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> veiculoService.atualizar(id, request));
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve transferir propriedade do veículo")
    void deveTransferirPropriedadeDoVeiculo() {
        // Arrange
        Long veiculoId = 1L;
        Long novoClienteId = 2L;

        Cliente clienteAntigo = new Cliente();
        clienteAntigo.setId(1L);

        Cliente clienteNovo = new Cliente();
        clienteNovo.setId(novoClienteId);

        Veiculo entity = new Veiculo();
        entity.setId(veiculoId);
        entity.setPlaca("ABC1D23");
        entity.setCliente(clienteAntigo);

        Veiculo updatedEntity = new Veiculo();
        updatedEntity.setId(veiculoId);
        updatedEntity.setPlaca("ABC1D23");
        updatedEntity.setCliente(clienteNovo);

        VeiculoResponseDTO response = new VeiculoResponseDTO();
        response.setId(veiculoId);
        response.setPlaca("ABC1D23");
        response.setClienteId(novoClienteId);

        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(entity));
        when(clienteService.getCliente(novoClienteId)).thenReturn(clienteNovo);
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(updatedEntity);
        when(veiculoMapper.toDTO(updatedEntity)).thenReturn(response);

        // Act
        VeiculoResponseDTO result = veiculoService.transferirPropriedade(veiculoId, novoClienteId);

        // Assert
        assertNotNull(result);
        assertEquals(novoClienteId, result.getClienteId());
        verify(clienteService, times(1)).getCliente(novoClienteId);
        verify(veiculoRepository, times(1)).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve deletar veículo (soft delete)")
    void deveDeletarVeiculo() {
        // Arrange
        Long id = 1L;
        Veiculo entity = new Veiculo();
        entity.setId(id);
        entity.setAtivo(true);

        when(veiculoRepository.findById(id)).thenReturn(Optional.of(entity));
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(entity);

        // Act
        veiculoService.deletar(id);

        // Assert
        verify(veiculoRepository, times(1)).findById(id);
        verify(veiculoRepository, times(1)).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve obter veículo por ID")
    void deveObterVeiculoPorId() {
        // Arrange
        Long id = 1L;
        Veiculo entity = new Veiculo();
        entity.setId(id);
        entity.setPlaca("ABC1D23");

        when(veiculoRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        Veiculo result = veiculoService.getVeiculo(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(veiculoRepository, times(1)).findById(id);
    }
}