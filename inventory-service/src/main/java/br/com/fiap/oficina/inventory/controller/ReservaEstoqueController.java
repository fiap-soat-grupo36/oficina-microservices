package br.com.fiap.oficina.inventory.controller;

import br.com.fiap.oficina.inventory.dto.request.ReservaLoteRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.ReservaLoteResponseDTO;
import br.com.fiap.oficina.inventory.service.ReservaEstoqueService;
import br.com.fiap.oficina.inventory.service.ReservaEstoqueLoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
@Profile({"dev", "local"})
public class ReservaEstoqueController {

    private final ReservaEstoqueLoteService reservaEstoqueLoteService;
    private final ReservaEstoqueService reservaEstoqueService;

    @PostMapping("/lote")
    @PreAuthorize("hasRole('ATENDENTE')")
    public ResponseEntity<ReservaLoteResponseDTO> reservarEmLote(
            @Valid @RequestBody ReservaLoteRequestDTO request) {
        ReservaLoteResponseDTO response = reservaEstoqueLoteService.reservarEmLote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/os/{osId}")
    @PreAuthorize("hasRole('ATENDENTE')")
    public ResponseEntity<Void> cancelarReservasPorOrdemServico(@PathVariable Long osId) {
        reservaEstoqueService.cancelarPorOrdemServico(osId);
        return ResponseEntity.noContent().build();
    }
}
