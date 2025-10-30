package br.com.fiap.oficina.inventory.controller;

import br.com.fiap.oficina.inventory.dto.response.ProdutoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.service.ProdutoEstoqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estoque/saldo")
@RequiredArgsConstructor
public class ProdutoEstoqueController {

    private final ProdutoEstoqueService produtoEstoqueService;

    @GetMapping("/{produtoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA', 'ATENDENTE')")
    public ResponseEntity<ProdutoEstoqueResponseDTO> obterSaldo(@PathVariable Long produtoId) {
        ProdutoEstoqueResponseDTO saldo = produtoEstoqueService.getSaldoConsolidado(produtoId);
        return ResponseEntity.ok(saldo);
    }
}
