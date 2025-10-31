package br.com.fiap.oficina.budget.controller;

import br.com.fiap.oficina.budget.dto.response.OrcamentoResponseDTO;
import br.com.fiap.oficina.budget.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
@Tag(name = "Orçamentos", description = "Endpoints para gerenciamento de orçamentos")
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    @Autowired
    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    @Operation(summary = "Listar todos os orçamentos", description = "Retorna lista completa de orçamentos")
    public ResponseEntity<List<OrcamentoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(orcamentoService.buscarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    @Operation(summary = "Buscar orçamento por ID", description = "Retorna um orçamento específico")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(orcamentoService.buscarPorId(id));
    }

    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    @Operation(summary = "Aprovar orçamento", description = "Aprova um orçamento específico")
    public ResponseEntity<OrcamentoResponseDTO> aprovar(@PathVariable Long id) {
        return ResponseEntity.ok(orcamentoService.aprovar(id));
    }

    @PutMapping("/{id}/reprovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    @Operation(summary = "Reprovar orçamento", description = "Reprova um orçamento específico")
    public ResponseEntity<OrcamentoResponseDTO> reprovar(@PathVariable Long id) {
        return ResponseEntity.ok(orcamentoService.reprovar(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(
        summary = "Deletar orçamento",
        description = "Remove um orçamento do sistema"
    )
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        orcamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ordem-servico/{ordemServicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    @Operation(
        summary = "Buscar orçamento por ordem de serviço",
        description = "Retorna o orçamento associado a uma ordem de serviço específica"
    )
    public ResponseEntity<OrcamentoResponseDTO> buscarPorOrdemServico(
        @PathVariable Long ordemServicoId
    ) {
        return ResponseEntity.ok(orcamentoService.buscarPorOrdemServicoId(ordemServicoId));
    }
}
