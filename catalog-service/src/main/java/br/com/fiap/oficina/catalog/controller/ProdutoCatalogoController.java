package br.com.fiap.oficina.catalog.controller;

import br.com.fiap.oficina.catalog.dto.request.ProdutoCatalogoRequestDTO;
import br.com.fiap.oficina.catalog.dto.response.ProdutoCatalogoResponseDTO;
import br.com.fiap.oficina.catalog.service.ProdutoCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/catalogo-produtos")
@Tag(
        name = "Catálogo de Produtos",
        description = "Gerenciamento de produtos/peças do catálogo."
)
public class ProdutoCatalogoController {

    private final ProdutoCatalogoService produtoService;

    @Autowired
    public ProdutoCatalogoController(ProdutoCatalogoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar novo produto no catálogo.",
            description = "Cria um novo produto no catálogo com base nos dados fornecidos.",
            operationId = "criarProdutoCatalogo"
    )
    @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso.")
    public ResponseEntity<ProdutoCatalogoResponseDTO> cadastrar(@RequestBody @Valid ProdutoCatalogoRequestDTO produto) {
        ProdutoCatalogoResponseDTO salvo = produtoService.salvar(produto);
        URI location = URI.create("/api/catalogo-produtos/" + salvo.getId());
        return ResponseEntity.created(location).body(salvo);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os produtos do catálogo.",
            description = "Retorna uma lista de todos os produtos cadastrados no catálogo.",
            operationId = "listarProdutosCatalogo"
    )
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<List<ProdutoCatalogoResponseDTO>> listarTodos() {
        List<ProdutoCatalogoResponseDTO> produtos = produtoService.listarTodos();
        if (produtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/ativos")
    @Operation(
            summary = "Listar produtos ativos.",
            description = "Retorna uma lista de produtos ativos no catálogo.",
            operationId = "listarProdutosAtivos"
    )
    @ApiResponse(responseCode = "200", description = "Lista de produtos ativos retornada com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<List<ProdutoCatalogoResponseDTO>> listarAtivos() {
        List<ProdutoCatalogoResponseDTO> produtos = produtoService.listarAtivos();
        if (produtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar produtos por termo.",
            description = "Retorna lista de produtos que contêm o termo no nome ou descrição.",
            operationId = "buscarProdutosPorTermo"
    )
    @ApiResponse(responseCode = "200", description = "Produtos encontrados com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<List<ProdutoCatalogoResponseDTO>> buscarPorTermo(@RequestParam String termo) {
        List<ProdutoCatalogoResponseDTO> produtos = produtoService.buscarPorTermo(termo);
        if (produtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar produto por ID.",
            description = "Retorna os detalhes de um produto específico do catálogo com base no ID informado.",
            operationId = "buscarProdutoCatalogoPorId"
    )
    @ApiResponse(responseCode = "200", description = "Produto encontrado com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<ProdutoCatalogoResponseDTO> buscarPorId(@PathVariable Long id) {
        ProdutoCatalogoResponseDTO produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(produto);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar produto por ID.",
            description = "Atualiza os dados de um produto do catálogo com base no ID informado.",
            operationId = "atualizarProdutoCatalogo"
    )
    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<ProdutoCatalogoResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid ProdutoCatalogoRequestDTO produto) {
        ProdutoCatalogoResponseDTO atualizado = produtoService.atualizar(id, produto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar produto por ID.",
            description = "Remove um produto do catálogo com base no ID informado (hard delete).",
            operationId = "deletarProdutoCatalogo"
    )
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
