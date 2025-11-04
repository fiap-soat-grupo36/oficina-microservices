package br.com.fiap.oficina.inventory.controller;

import br.com.fiap.oficina.inventory.dto.response.ProdutoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.service.ProdutoEstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estoque")
@RequiredArgsConstructor
@Tag(name = "Produto Estoque", description = "Gerenciamento de produtos em estoque")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoEstoqueController {

    private final ProdutoEstoqueService produtoEstoqueService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'ESTOQUISTA')")
    @Operation(
            summary = "Listar todos os produtos em estoque",
            description = "Retorna lista completa de produtos no estoque"
    )
    public ResponseEntity<List<ProdutoEstoqueResponseDTO>> listarTodos() {
        List<ProdutoEstoqueResponseDTO> estoque = produtoEstoqueService.listarTodos();
        if (estoque.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(estoque);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'ESTOQUISTA')")
    @Operation(
            summary = "Buscar produto em estoque por ID",
            description = "Retorna detalhes de um produto específico no estoque"
    )
    public ResponseEntity<ProdutoEstoqueResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoEstoqueService.buscarPorId(id));
    }

    @GetMapping("/produto/{produtoCatalogoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'ESTOQUISTA')")
    @Operation(
            summary = "Buscar estoque por produto do catálogo",
            description = "Retorna o estoque de um produto específico do catálogo"
    )
    public ResponseEntity<ProdutoEstoqueResponseDTO> buscarPorProdutoCatalogo(
            @PathVariable Long produtoCatalogoId
    ) {
        return ResponseEntity.ok(produtoEstoqueService.buscarPorProdutoCatalogo(produtoCatalogoId));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'ESTOQUISTA')")
    @Operation(
            summary = "Buscar produtos em estoque por termo",
            description = "Busca produtos cujo nome contenha o termo informado"
    )
    public ResponseEntity<List<ProdutoEstoqueResponseDTO>> buscarPorTermo(
            @RequestParam String termo
    ) {
        List<ProdutoEstoqueResponseDTO> produtos = produtoEstoqueService.buscarPorTermo(termo);
        if (produtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/baixo-estoque")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'ESTOQUISTA')")
    @Operation(
            summary = "Listar produtos com estoque baixo",
            description = "Retorna produtos com quantidade abaixo do estoque mínimo"
    )
    public ResponseEntity<List<ProdutoEstoqueResponseDTO>> listarBaixoEstoque() {
        List<ProdutoEstoqueResponseDTO> produtos = produtoEstoqueService.listarBaixoEstoque();
        if (produtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(produtos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    @Operation(
            summary = "Atualizar estoque mínimo",
            description = "Atualiza a quantidade mínima de estoque de um produto"
    )
    public ResponseEntity<ProdutoEstoqueResponseDTO> atualizarEstoqueMinimo(
            @PathVariable Long id,
            @RequestParam Integer estoqueMinimo
    ) {
        return ResponseEntity.ok(produtoEstoqueService.atualizarEstoqueMinimo(id, estoqueMinimo));
    }

    // Legacy endpoint for backward compatibility
    @GetMapping("/saldo/{produtoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA', 'ATENDENTE')")
    @Operation(
            summary = "Obter saldo consolidado (legado)",
            description = "Endpoint legado para obter saldo consolidado"
    )
    public ResponseEntity<ProdutoEstoqueResponseDTO> obterSaldo(@PathVariable Long produtoId) {
        ProdutoEstoqueResponseDTO saldo = produtoEstoqueService.getSaldoConsolidado(produtoId);
        return ResponseEntity.ok(saldo);
    }
}
