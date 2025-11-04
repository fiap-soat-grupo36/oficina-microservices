package br.com.fiap.oficina.inventory.controller;

import br.com.fiap.oficina.inventory.dto.request.MovimentacaoEstoqueRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.MovimentacaoEstoqueResponseDTO;
import br.com.fiap.oficina.inventory.service.MovimentacaoEstoqueService;
import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/estoque/movimentacoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public ResponseEntity<MovimentacaoEstoqueResponseDTO> registrarMovimentacao(
            @Valid @RequestBody MovimentacaoEstoqueRequestDTO request) {
        MovimentacaoEstoqueResponseDTO response;

        if (request.getTipoMovimentacao() == TipoMovimentacao.ENTRADA) {
            response = movimentacaoEstoqueService.registrarEntrada(request);
        } else {
            response = movimentacaoEstoqueService.registrarSaida(request);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA', 'ATENDENTE')")
    public ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarMovimentacoes(
            @RequestParam(required = false) Long produtoCatalogoId,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        List<MovimentacaoEstoqueResponseDTO> movimentacoes =
                movimentacaoEstoqueService.listarMovimentacoes(produtoCatalogoId, tipo, dataInicio, dataFim);
        return ResponseEntity.ok(movimentacoes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA', 'ATENDENTE')")
    public ResponseEntity<MovimentacaoEstoqueResponseDTO> buscarPorId(@PathVariable Long id) {
        MovimentacaoEstoqueResponseDTO movimentacao = movimentacaoEstoqueService.buscarPorId(id);
        return ResponseEntity.ok(movimentacao);
    }
}
