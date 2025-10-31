package br.com.fiap.oficina.inventory.controller;

import br.com.fiap.oficina.inventory.dto.request.ProdutoEntradaEstoqueRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.ProdutoEntradaEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.service.ProdutoEntradaEstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/estoque/entradas")
@RequiredArgsConstructor
@Tag(name = "Produto Entrada Estoque", description = "Gerenciamento de entradas de produtos no estoque")
public class ProdutoEntradaEstoqueController {

    private final ProdutoEntradaEstoqueService entradaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    @Operation(
        summary = "Registrar entrada de produtos no estoque",
        description = "Registra a entrada de uma ou mais unidades de um produto"
    )
    public ResponseEntity<ProdutoEntradaEstoqueResponseDTO> registrarEntrada(
        @Valid @RequestBody ProdutoEntradaEstoqueRequestDTO request
    ) {
        ProdutoEntradaEstoqueResponseDTO response = entradaService.registrarEntrada(request);
        URI location = URI.create("/estoque/entradas/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA', 'ATENDENTE')")
    @Operation(
        summary = "Listar todas as entradas de estoque",
        description = "Retorna histórico de entradas de produtos"
    )
    public ResponseEntity<List<ProdutoEntradaEstoqueResponseDTO>> listarEntradas(
        @RequestParam(required = false) Long produtoCatalogoId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        List<ProdutoEntradaEstoqueResponseDTO> entradas = 
            entradaService.listarEntradas(produtoCatalogoId, dataInicio, dataFim);
        return ResponseEntity.ok(entradas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA', 'ATENDENTE')")
    @Operation(
        summary = "Buscar entrada de estoque por ID",
        description = "Retorna detalhes de uma entrada específica"
    )
    public ResponseEntity<ProdutoEntradaEstoqueResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(entradaService.buscarPorId(id));
    }
}
