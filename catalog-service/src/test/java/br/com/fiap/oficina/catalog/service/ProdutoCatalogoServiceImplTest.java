package br.com.fiap.oficina.catalog.service;

import br.com.fiap.oficina.catalog.dto.request.ProdutoCatalogoRequestDTO;
import br.com.fiap.oficina.catalog.dto.response.ProdutoCatalogoResponseDTO;
import br.com.fiap.oficina.catalog.entity.ProdutoCatalogo;
import br.com.fiap.oficina.catalog.mapper.ProdutoCatalogoMapper;
import br.com.fiap.oficina.catalog.repository.ProdutoCatalogoRepository;
import br.com.fiap.oficina.catalog.service.impl.ProdutoCatalogoServiceImpl;
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
class ProdutoCatalogoServiceImplTest {

    @Mock
    private ProdutoCatalogoRepository produtoCatalogoRepository;

    @Mock
    private ProdutoCatalogoMapper produtoCatalogoMapper;

    @InjectMocks
    private ProdutoCatalogoServiceImpl produtoCatalogoService;

    @Test
    @DisplayName("Deve salvar produto")
    void deveSalvarProduto() {
        // Arrange
        ProdutoCatalogoRequestDTO request = new ProdutoCatalogoRequestDTO();
        request.setNome("Filtro de Óleo");

        ProdutoCatalogo entity = new ProdutoCatalogo();
        entity.setNome("Filtro de Óleo");

        ProdutoCatalogo savedEntity = new ProdutoCatalogo();
        savedEntity.setId(1L);
        savedEntity.setNome("Filtro de Óleo");

        ProdutoCatalogoResponseDTO response = new ProdutoCatalogoResponseDTO();
        response.setId(1L);
        response.setNome("Filtro de Óleo");

        when(produtoCatalogoMapper.toEntity(request)).thenReturn(entity);
        when(produtoCatalogoRepository.save(any(ProdutoCatalogo.class))).thenReturn(savedEntity);
        when(produtoCatalogoMapper.toDTO(savedEntity)).thenReturn(response);

        // Act
        ProdutoCatalogoResponseDTO result = produtoCatalogoService.salvar(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(produtoCatalogoRepository, times(1)).save(any(ProdutoCatalogo.class));
    }

    @Test
    @DisplayName("Deve buscar produto por ID")
    void deveBuscarProdutoPorId() {
        // Arrange
        Long id = 1L;
        ProdutoCatalogo entity = new ProdutoCatalogo();
        entity.setId(id);
        entity.setNome("Filtro de Óleo");

        ProdutoCatalogoResponseDTO response = new ProdutoCatalogoResponseDTO();
        response.setId(id);
        response.setNome("Filtro de Óleo");

        when(produtoCatalogoRepository.findById(id)).thenReturn(Optional.of(entity));
        when(produtoCatalogoMapper.toDTO(entity)).thenReturn(response);

        // Act
        ProdutoCatalogoResponseDTO result = produtoCatalogoService.buscarPorId(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(produtoCatalogoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado")
    void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
        // Arrange
        Long id = 999L;
        when(produtoCatalogoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> produtoCatalogoService.buscarPorId(id));
        verify(produtoCatalogoRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve listar todos os produtos")
    void deveListarTodosOsProdutos() {
        // Arrange
        ProdutoCatalogo produto1 = new ProdutoCatalogo();
        produto1.setId(1L);

        ProdutoCatalogo produto2 = new ProdutoCatalogo();
        produto2.setId(2L);

        List<ProdutoCatalogo> produtos = Arrays.asList(produto1, produto2);

        ProdutoCatalogoResponseDTO response1 = new ProdutoCatalogoResponseDTO();
        response1.setId(1L);

        ProdutoCatalogoResponseDTO response2 = new ProdutoCatalogoResponseDTO();
        response2.setId(2L);

        List<ProdutoCatalogoResponseDTO> responses = Arrays.asList(response1, response2);

        when(produtoCatalogoRepository.findAll()).thenReturn(produtos);
        when(produtoCatalogoMapper.toDTO(produtos)).thenReturn(responses);

        // Act
        List<ProdutoCatalogoResponseDTO> result = produtoCatalogoService.listarTodos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(produtoCatalogoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve atualizar produto")
    void deveAtualizarProduto() {
        // Arrange
        Long id = 1L;
        ProdutoCatalogoRequestDTO request = new ProdutoCatalogoRequestDTO();
        request.setNome("Produto Atualizado");
        request.setPreco(BigDecimal.valueOf(100.00));

        ProdutoCatalogo existingEntity = new ProdutoCatalogo();
        existingEntity.setId(id);

        ProdutoCatalogo updatedEntity = new ProdutoCatalogo();
        updatedEntity.setId(id);
        updatedEntity.setNome("Produto Atualizado");

        ProdutoCatalogoResponseDTO response = new ProdutoCatalogoResponseDTO();
        response.setId(id);
        response.setNome("Produto Atualizado");

        when(produtoCatalogoRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(produtoCatalogoRepository.save(any(ProdutoCatalogo.class))).thenReturn(updatedEntity);
        when(produtoCatalogoMapper.toDTO(updatedEntity)).thenReturn(response);

        // Act
        ProdutoCatalogoResponseDTO result = produtoCatalogoService.atualizar(id, request);

        // Assert
        assertNotNull(result);
        assertEquals("Produto Atualizado", result.getNome());
        verify(produtoCatalogoRepository, times(1)).save(any(ProdutoCatalogo.class));
    }

    @Test
    @DisplayName("Deve deletar produto")
    void deveDeletarProduto() {
        // Arrange
        Long id = 1L;
        ProdutoCatalogo entity = new ProdutoCatalogo();
        entity.setId(id);
        entity.setAtivo(true);

        when(produtoCatalogoRepository.findById(id)).thenReturn(Optional.of(entity));
        when(produtoCatalogoRepository.save(any(ProdutoCatalogo.class))).thenReturn(entity);

        // Act
        produtoCatalogoService.deletar(id);

        // Assert
        verify(produtoCatalogoRepository, times(1)).findById(id);
        verify(produtoCatalogoRepository, times(1)).save(any(ProdutoCatalogo.class));
    }

    @Test
    @DisplayName("Deve listar produtos ativos")
    void deveListarProdutosAtivos() {
        // Arrange
        ProdutoCatalogo produto = new ProdutoCatalogo();
        produto.setId(1L);
        produto.setAtivo(true);

        List<ProdutoCatalogo> produtos = Arrays.asList(produto);
        
        ProdutoCatalogoResponseDTO response = new ProdutoCatalogoResponseDTO();
        response.setId(1L);

        when(produtoCatalogoRepository.findByAtivoTrue()).thenReturn(produtos);
        when(produtoCatalogoMapper.toDTO(produtos)).thenReturn(Arrays.asList(response));

        // Act
        List<ProdutoCatalogoResponseDTO> result = produtoCatalogoService.listarAtivos();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(produtoCatalogoRepository, times(1)).findByAtivoTrue();
    }
}
