package br.com.fiap.oficina.workorder.controller;

import br.com.fiap.oficina.shared.enums.StatusOrdemServico;
import br.com.fiap.oficina.workorder.dto.request.ItemOrdemServicoDTO;
import br.com.fiap.oficina.workorder.dto.request.OsRequestDTO;
import br.com.fiap.oficina.workorder.dto.response.OrdemServicoResumoDTO;
import br.com.fiap.oficina.workorder.dto.response.OrdemServicoResponseDTO;
import br.com.fiap.oficina.workorder.dto.response.OsItemDTO;
import br.com.fiap.oficina.workorder.service.OrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordens-servico")
@RequiredArgsConstructor
@Tag(name = "Ordens de Serviço", description = "Gerenciamento de ordens de serviço")
public class OrdemServicoController {

    private final OrdemServicoService service;

    @PostMapping
    @Operation(summary = "Criar ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> criar(@RequestBody @Valid OsRequestDTO request) {
        OrdemServicoResponseDTO response = service.criar(request);
        URI location = URI.create("/api/ordens-servico/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as ordens de serviço")
    public ResponseEntity<List<OrdemServicoResponseDTO>> listarTodos(
            @RequestParam(required = false) List<StatusOrdemServico> status) {
        return ResponseEntity.ok(service.listarTodos(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ordem de serviço por ID")
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid OsRequestDTO request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar ordem de serviço")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/aprovadas")
    @Operation(summary = "Listar ordens de serviço aprovadas")
    public ResponseEntity<List<OrdemServicoResponseDTO>> listarAprovadas() {
        return ResponseEntity.ok(service.buscarAtualizadas());
    }

    @GetMapping("/por-cliente/{clienteId}")
    @Operation(summary = "Buscar ordens de serviço por cliente")
    public ResponseEntity<List<OrdemServicoResumoDTO>> buscarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.buscarPorCliente(clienteId));
    }

    @GetMapping("/por-veiculo/{veiculoId}")
    @Operation(summary = "Buscar ordens de serviço por veículo")
    public ResponseEntity<List<OrdemServicoResumoDTO>> buscarPorVeiculo(@PathVariable Long veiculoId) {
        return ResponseEntity.ok(service.buscarPorVeiculo(veiculoId));
    }

    @GetMapping("/por-mecanico/{mecanicoId}")
    @Operation(summary = "Buscar ordens de serviço por mecânico")
    public ResponseEntity<List<OrdemServicoResponseDTO>> buscarPorMecanico(@PathVariable Long mecanicoId) {
        return ResponseEntity.ok(service.buscarPorMecanico(mecanicoId));
    }

    @PutMapping("/{id}/atribuir-mecanico")
    @Operation(summary = "Atribuir mecânico à ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> atribuirMecanico(
            @PathVariable Long id,
            @RequestParam Long mecanicoId) {
        return ResponseEntity.ok(service.atribuirMecanico(id, mecanicoId));
    }

    @PutMapping("/{id}/diagnosticar")
    @Operation(summary = "Diagnosticar ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> diagnosticar(
            @PathVariable Long id,
            @RequestBody(required = false) String observacoes) {
        return ResponseEntity.ok(service.diagnosticar(id, observacoes));
    }

    @PutMapping("/{id}/executar")
    @Operation(summary = "Iniciar execução da ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> executar(
            @PathVariable Long id,
            @RequestBody(required = false) String observacoes) {
        return ResponseEntity.ok(service.executar(id, observacoes));
    }

    @PutMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> finalizar(
            @PathVariable Long id,
            @RequestBody(required = false) String observacoes) {
        return ResponseEntity.ok(service.finalizar(id, observacoes));
    }

    @PutMapping("/{id}/entregar")
    @Operation(summary = "Entregar veículo")
    public ResponseEntity<OrdemServicoResponseDTO> entregar(
            @PathVariable Long id,
            @RequestBody(required = false) String observacoes) {
        return ResponseEntity.ok(service.entregar(id, observacoes));
    }

    @PostMapping("/{id}/servicos")
    @Operation(summary = "Adicionar serviços à ordem de serviço")
    public ResponseEntity<List<OsItemDTO>> adicionarServicos(
            @PathVariable Long id,
            @RequestBody List<Long> servicosIds) {
        return ResponseEntity.ok(service.adicionarServicos(id, servicosIds));
    }

    @DeleteMapping("/{id}/servicos")
    @Operation(summary = "Remover serviços da ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> removerServicos(
            @PathVariable Long id,
            @RequestBody List<Long> servicosIds) {
        return ResponseEntity.ok(service.removerServicos(id, servicosIds));
    }

    @PostMapping("/{id}/produtos")
    @Operation(summary = "Adicionar produtos à ordem de serviço")
    public ResponseEntity<List<OsItemDTO>> adicionarProdutos(
            @PathVariable Long id,
            @RequestBody List<ItemOrdemServicoDTO> produtos) {
        return ResponseEntity.ok(service.adicionarProdutos(id, produtos));
    }

    @DeleteMapping("/{id}/produtos")
    @Operation(summary = "Remover produtos da ordem de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> removerProdutos(
            @PathVariable Long id,
            @RequestBody List<Long> produtosIds) {
        return ResponseEntity.ok(service.removerProdutos(id, produtosIds));
    }

    @GetMapping("/{id}/orcamento")
    @Operation(summary = "Buscar orçamento da ordem de serviço")
    public ResponseEntity<Object> buscarOrcamento(@PathVariable Long id) {
        OrdemServicoResponseDTO os = service.buscarPorId(id);
        return ResponseEntity.ok(os.getOrcamento());
    }
}
