package br.com.fiap.oficina.inventory.service;

import br.com.fiap.oficina.inventory.dto.response.ProdutoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.entity.MovimentacaoEstoque;
import br.com.fiap.oficina.inventory.entity.ProdutoEstoque;
import br.com.fiap.oficina.inventory.entity.ReservaEstoque;
import br.com.fiap.oficina.inventory.mapper.ProdutoEstoqueMapper;
import br.com.fiap.oficina.inventory.repository.MovimentacaoEstoqueRepository;
import br.com.fiap.oficina.inventory.repository.ProdutoEstoqueRepository;
import br.com.fiap.oficina.inventory.repository.ReservaEstoqueRepository;
import br.com.fiap.oficina.inventory.service.impl.ProdutoEstoqueServiceImpl;
import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoEstoqueServiceImplTest {

    @Mock
    private ProdutoEstoqueRepository produtoEstoqueRepository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Mock
    private ReservaEstoqueRepository reservaEstoqueRepository;

    @Mock
    private ProdutoEstoqueMapper produtoEstoqueMapper;

    @InjectMocks
    private ProdutoEstoqueServiceImpl produtoEstoqueService;

    @Test
    @DisplayName("Deve obter saldo existente")
    void deveObterSaldoExistente() {
        // Arrange
        Long produtoCatalogoId = 100L;
        ProdutoEstoque produtoEstoque = new ProdutoEstoque();
        produtoEstoque.setId(1L);
        produtoEstoque.setProdutoCatalogoId(produtoCatalogoId);
        produtoEstoque.setQuantidadeTotal(50);

        when(produtoEstoqueRepository.findByProdutoCatalogoId(produtoCatalogoId))
                .thenReturn(Optional.of(produtoEstoque));

        // Act
        ProdutoEstoque result = produtoEstoqueService.obterOuCriarSaldo(produtoCatalogoId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(50, result.getQuantidadeTotal());
        verify(produtoEstoqueRepository, times(1)).findByProdutoCatalogoId(produtoCatalogoId);
        verify(produtoEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar novo saldo quando não existir")
    void deveCriarNovoSaldoQuandoNaoExistir() {
        // Arrange
        Long produtoCatalogoId = 100L;
        ProdutoEstoque novoProduto = new ProdutoEstoque();
        novoProduto.setId(1L);
        novoProduto.setProdutoCatalogoId(produtoCatalogoId);

        when(produtoEstoqueRepository.findByProdutoCatalogoId(produtoCatalogoId))
                .thenReturn(Optional.empty());
        when(produtoEstoqueRepository.save(any(ProdutoEstoque.class)))
                .thenReturn(novoProduto);

        // Act
        ProdutoEstoque result = produtoEstoqueService.obterOuCriarSaldo(produtoCatalogoId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(produtoEstoqueRepository, times(1)).findByProdutoCatalogoId(produtoCatalogoId);
        verify(produtoEstoqueRepository, times(1)).save(any(ProdutoEstoque.class));
    }

    @Test
    @DisplayName("Deve atualizar saldo após movimentação")
    void deveAtualizarSaldoAposMovimentacao() {
        // Arrange
        Long produtoCatalogoId = 100L;
        ProdutoEstoque produtoEstoque = new ProdutoEstoque();
        produtoEstoque.setId(1L);
        produtoEstoque.setProdutoCatalogoId(produtoCatalogoId);

        MovimentacaoEstoque entrada = new MovimentacaoEstoque();
        entrada.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        entrada.setQuantidade(50);
        entrada.setPrecoUnitario(BigDecimal.TEN);

        MovimentacaoEstoque saida = new MovimentacaoEstoque();
        saida.setTipoMovimentacao(TipoMovimentacao.SAIDA);
        saida.setQuantidade(10);

        List<MovimentacaoEstoque> movimentacoes = Arrays.asList(entrada, saida);

        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setQuantidadeReservada(5);

        when(produtoEstoqueRepository.findByProdutoCatalogoId(produtoCatalogoId))
                .thenReturn(Optional.of(produtoEstoque));
        when(movimentacaoEstoqueRepository.findByProdutoCatalogoId(produtoCatalogoId))
                .thenReturn(movimentacoes);
        when(reservaEstoqueRepository.findByProdutoCatalogoIdAndAtivaTrue(produtoCatalogoId))
                .thenReturn(Arrays.asList(reserva));
        when(produtoEstoqueRepository.save(any(ProdutoEstoque.class)))
                .thenReturn(produtoEstoque);

        // Act
        produtoEstoqueService.atualizarSaldoAposMovimentacao(produtoCatalogoId);

        // Assert
        verify(produtoEstoqueRepository, times(2)).save(any(ProdutoEstoque.class));
        assertEquals(40, produtoEstoque.getQuantidadeTotal()); // 50 - 10
        assertEquals(5, produtoEstoque.getQuantidadeReservada());
        assertEquals(35, produtoEstoque.getQuantidadeDisponivel()); // 40 - 5
    }

    @Test
    @DisplayName("Deve listar todos os produtos em estoque")
    void deveListarTodosProdutosEmEstoque() {
        // Arrange
        ProdutoEstoque produto1 = new ProdutoEstoque();
        produto1.setId(1L);

        ProdutoEstoque produto2 = new ProdutoEstoque();
        produto2.setId(2L);

        List<ProdutoEstoque> produtos = Arrays.asList(produto1, produto2);

        ProdutoEstoqueResponseDTO dto1 = new ProdutoEstoqueResponseDTO();
        dto1.setId(1L);

        ProdutoEstoqueResponseDTO dto2 = new ProdutoEstoqueResponseDTO();
        dto2.setId(2L);

        when(produtoEstoqueRepository.findAll()).thenReturn(produtos);
        when(produtoEstoqueMapper.toResponseDTO(produto1)).thenReturn(dto1);
        when(produtoEstoqueMapper.toResponseDTO(produto2)).thenReturn(dto2);

        // Act
        List<ProdutoEstoqueResponseDTO> result = produtoEstoqueService.listarTodos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(produtoEstoqueRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar produto por ID")
    void deveBuscarProdutoPorId() {
        // Arrange
        Long id = 1L;
        ProdutoEstoque produto = new ProdutoEstoque();
        produto.setId(id);

        ProdutoEstoqueResponseDTO dto = new ProdutoEstoqueResponseDTO();
        dto.setId(id);

        when(produtoEstoqueRepository.findById(id)).thenReturn(Optional.of(produto));
        when(produtoEstoqueMapper.toResponseDTO(produto)).thenReturn(dto);

        // Act
        ProdutoEstoqueResponseDTO result = produtoEstoqueService.buscarPorId(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(produtoEstoqueRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado por ID")
    void deveLancarExcecaoQuandoProdutoNaoEncontradoPorId() {
        // Arrange
        Long id = 999L;
        when(produtoEstoqueRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, 
                () -> produtoEstoqueService.buscarPorId(id));
        verify(produtoEstoqueRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve buscar produto por produto catálogo")
    void deveBuscarProdutoPorProdutoCatalogo() {
        // Arrange
        Long produtoCatalogoId = 100L;
        ProdutoEstoque produto = new ProdutoEstoque();
        produto.setProdutoCatalogoId(produtoCatalogoId);

        ProdutoEstoqueResponseDTO dto = new ProdutoEstoqueResponseDTO();
        dto.setProdutoCatalogoId(produtoCatalogoId);

        when(produtoEstoqueRepository.findByProdutoCatalogoId(produtoCatalogoId))
                .thenReturn(Optional.of(produto));
        when(produtoEstoqueMapper.toResponseDTO(produto)).thenReturn(dto);

        // Act
        ProdutoEstoqueResponseDTO result = produtoEstoqueService.buscarPorProdutoCatalogo(produtoCatalogoId);

        // Assert
        assertNotNull(result);
        assertEquals(produtoCatalogoId, result.getProdutoCatalogoId());
        verify(produtoEstoqueRepository, times(1)).findByProdutoCatalogoId(produtoCatalogoId);
    }

    @Test
    @DisplayName("Deve listar produtos com baixo estoque")
    void deveListarProdutosComBaixoEstoque() {
        // Arrange
        ProdutoEstoque produto = new ProdutoEstoque();
        produto.setId(1L);
        produto.setQuantidadeTotal(5);
        produto.setEstoqueMinimo(10);

        ProdutoEstoqueResponseDTO dto = new ProdutoEstoqueResponseDTO();
        dto.setId(1L);

        when(produtoEstoqueRepository.findBaixoEstoque()).thenReturn(Arrays.asList(produto));
        when(produtoEstoqueMapper.toResponseDTO(produto)).thenReturn(dto);

        // Act
        List<ProdutoEstoqueResponseDTO> result = produtoEstoqueService.listarBaixoEstoque();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(produtoEstoqueRepository, times(1)).findBaixoEstoque();
    }

    @Test
    @DisplayName("Deve atualizar estoque mínimo")
    void deveAtualizarEstoqueMinimo() {
        // Arrange
        Long id = 1L;
        Integer novoEstoqueMinimo = 20;
        ProdutoEstoque produto = new ProdutoEstoque();
        produto.setId(id);
        produto.setEstoqueMinimo(10);

        ProdutoEstoqueResponseDTO dto = new ProdutoEstoqueResponseDTO();
        dto.setId(id);
        dto.setEstoqueMinimo(novoEstoqueMinimo);

        when(produtoEstoqueRepository.findById(id)).thenReturn(Optional.of(produto));
        when(produtoEstoqueRepository.save(any(ProdutoEstoque.class))).thenReturn(produto);
        when(produtoEstoqueMapper.toResponseDTO(produto)).thenReturn(dto);

        // Act
        ProdutoEstoqueResponseDTO result = produtoEstoqueService.atualizarEstoqueMinimo(id, novoEstoqueMinimo);

        // Assert
        assertNotNull(result);
        assertEquals(novoEstoqueMinimo, produto.getEstoqueMinimo());
        verify(produtoEstoqueRepository, times(1)).save(any(ProdutoEstoque.class));
    }

    @Test
    @DisplayName("Deve recalcular preço médio corretamente")
    void deveRecalcularPrecoMedioCorretamente() {
        // Arrange
        Long produtoCatalogoId = 100L;
        ProdutoEstoque produto = new ProdutoEstoque();
        produto.setProdutoCatalogoId(produtoCatalogoId);
        produto.setQuantidadeTotal(100);

        MovimentacaoEstoque entrada1 = new MovimentacaoEstoque();
        entrada1.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        entrada1.setQuantidade(50);
        entrada1.setPrecoUnitario(BigDecimal.TEN);

        MovimentacaoEstoque entrada2 = new MovimentacaoEstoque();
        entrada2.setTipoMovimentacao(TipoMovimentacao.ENTRADA);
        entrada2.setQuantidade(50);
        entrada2.setPrecoUnitario(BigDecimal.valueOf(20));

        when(produtoEstoqueRepository.findByProdutoCatalogoId(produtoCatalogoId))
                .thenReturn(Optional.of(produto));
        when(movimentacaoEstoqueRepository.findByProdutoCatalogoId(produtoCatalogoId))
                .thenReturn(Arrays.asList(entrada1, entrada2));
        when(produtoEstoqueRepository.save(any(ProdutoEstoque.class)))
                .thenReturn(produto);

        // Act
        produtoEstoqueService.recalcularPrecoMedio(produtoCatalogoId);

        // Assert
        verify(produtoEstoqueRepository, times(1)).save(any(ProdutoEstoque.class));
    }

    @Test
    @DisplayName("Deve obter saldo consolidado")
    void deveObterSaldoConsolidado() {
        // Arrange
        Long produtoCatalogoId = 100L;
        ProdutoEstoque produto = new ProdutoEstoque();
        produto.setProdutoCatalogoId(produtoCatalogoId);
        produto.setQuantidadeTotal(50);
        produto.setQuantidadeDisponivel(45);

        ProdutoEstoqueResponseDTO dto = new ProdutoEstoqueResponseDTO();
        dto.setProdutoCatalogoId(produtoCatalogoId);

        when(produtoEstoqueRepository.findByProdutoCatalogoId(produtoCatalogoId))
                .thenReturn(Optional.of(produto));
        when(produtoEstoqueMapper.toResponseDTO(produto)).thenReturn(dto);

        // Act
        ProdutoEstoqueResponseDTO result = produtoEstoqueService.getSaldoConsolidado(produtoCatalogoId);

        // Assert
        assertNotNull(result);
        assertEquals(produtoCatalogoId, result.getProdutoCatalogoId());
        verify(produtoEstoqueRepository, times(1)).findByProdutoCatalogoId(produtoCatalogoId);
    }
}
