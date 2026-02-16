package br.com.fiap.oficina.workorder.controller;

import br.com.fiap.oficina.shared.enums.StatusOrdemServico;
import br.com.fiap.oficina.workorder.dto.request.ItemOrdemServicoDTO;
import br.com.fiap.oficina.workorder.dto.request.OsRequestDTO;
import br.com.fiap.oficina.workorder.dto.response.OrcamentoResumoDTO;
import br.com.fiap.oficina.workorder.dto.response.OrdemServicoResponseDTO;
import br.com.fiap.oficina.workorder.dto.response.OrdemServicoResumoDTO;
import br.com.fiap.oficina.workorder.dto.response.OsItemDTO;
import br.com.fiap.oficina.workorder.service.OrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordens-servico")
@RequiredArgsConstructor
@Tag(name = "Ordens de Serviço", description = "Gerenciamento de ordens de serviço")
@SecurityRequirement(name = "bearerAuth")
public class OrdemServicoController {

    private final OrdemServicoService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Criar ordem de serviço", description = "Cria uma nova ordem de serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ordem de serviço criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente ou veículo não encontrado")
    })
    public ResponseEntity<OrdemServicoResponseDTO> criar(@RequestBody @Valid OsRequestDTO request) {
        OrdemServicoResponseDTO response = service.criar(request);
        URI location = URI.create("/api/ordens-servico/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO', 'CLIENTE')")
    @Operation(summary = "Listar todas as ordens de serviço", description = "Lista todas as ordens de serviço, com filtro opcional por status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<OrdemServicoResponseDTO>> listarTodos(
            @Parameter(description = "Lista de status para filtrar") @RequestParam(required = false) List<StatusOrdemServico> status) {
        return ResponseEntity.ok(service.listarTodos(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO', 'CLIENTE')")
    @Operation(summary = "Buscar ordem de serviço por ID", description = "Busca uma ordem de serviço específica pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem de serviço encontrada"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<OrdemServicoResponseDTO> buscarPorId(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Atualizar ordem de serviço", description = "Atualiza informações de uma ordem de serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem de serviço atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<OrdemServicoResponseDTO> atualizar(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id,
            @RequestBody @Valid OsRequestDTO request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar ordem de serviço", description = "Remove uma ordem de serviço do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ordem de serviço deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/aprovadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Listar ordens de serviço aprovadas", description = "Lista todas as ordens de serviço com status aprovado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<OrdemServicoResponseDTO>> listarAprovadas() {
        return ResponseEntity.ok(service.buscarAtualizadas());
    }

    @GetMapping("/por-cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    @Operation(summary = "Buscar ordens de serviço por cliente", description = "Lista todas as ordens de serviço de um cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<OrdemServicoResumoDTO>> buscarPorCliente(
            @Parameter(description = "ID do cliente") @PathVariable Long clienteId) {
        return ResponseEntity.ok(service.buscarPorCliente(clienteId));
    }

    @GetMapping("/por-veiculo/{veiculoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Buscar ordens de serviço por veículo", description = "Lista todas as ordens de serviço de um veículo específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<OrdemServicoResumoDTO>> buscarPorVeiculo(
            @Parameter(description = "ID do veículo") @PathVariable Long veiculoId) {
        return ResponseEntity.ok(service.buscarPorVeiculo(veiculoId));
    }

    @GetMapping("/por-mecanico/{mecanicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Buscar ordens de serviço por mecânico", description = "Lista todas as ordens de serviço atribuídas a um mecânico específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<OrdemServicoResponseDTO>> buscarPorMecanico(
            @Parameter(description = "ID do mecânico") @PathVariable Long mecanicoId) {
        return ResponseEntity.ok(service.buscarPorMecanico(mecanicoId));
    }

    @PutMapping("/{id}/atribuir-mecanico")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Atribuir mecânico à ordem de serviço", description = "Atribui um mecânico a uma ordem de serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mecânico atribuído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<OrdemServicoResponseDTO> atribuirMecanico(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id,
            @Parameter(description = "ID do mecânico") @RequestParam Long mecanicoId) {
        return ResponseEntity.ok(service.atribuirMecanico(id, mecanicoId));
    }

    @PutMapping("/{id}/diagnosticar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    @Operation(summary = "Realizar diagnóstico da ordem de serviço", description = "Realiza o diagnóstico e atualiza o status para EM_DIAGNOSTICO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Diagnóstico realizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<OrdemServicoResponseDTO> diagnosticar(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id,
            @Parameter(description = "Observações do diagnóstico") @RequestParam(required = false) String observacoes) {
        return ResponseEntity.ok(service.diagnosticar(id, observacoes));
    }

    @PutMapping("/{id}/executar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    @Operation(summary = "Iniciar execução da ordem de serviço", description = "Inicia a execução da ordem de serviço e atualiza o status para EM_EXECUCAO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Execução iniciada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<OrdemServicoResponseDTO> executar(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id,
            @Parameter(description = "Observações sobre a execução") @RequestParam(required = false) String observacoes) {
        return ResponseEntity.ok(service.executar(id, observacoes));
    }

    @PutMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    @Operation(summary = "Finalizar ordem de serviço", description = "Finaliza a ordem de serviço e atualiza o status para FINALIZADA")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem de serviço finalizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<OrdemServicoResponseDTO> finalizar(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id,
            @Parameter(description = "Observações sobre a finalização") @RequestParam(required = false) String observacoes) {
        return ResponseEntity.ok(service.finalizar(id, observacoes));
    }

    @PutMapping("/{id}/entregar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Registrar entrega do veículo ao cliente", description = "Registra a entrega do veículo ao cliente e atualiza o status para ENTREGUE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entrega registrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<OrdemServicoResponseDTO> entregar(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id,
            @Parameter(description = "Observações sobre a entrega") @RequestParam(required = false) String observacoes) {
        return ResponseEntity.ok(service.entregar(id, observacoes));
    }

    @PostMapping("/{id}/servicos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Adicionar serviços à ordem de serviço", description = "Adiciona serviços a uma ordem de serviço existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviços adicionados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não é possível adicionar serviços em ordem aguardando aprovação"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<List<OsItemDTO>> adicionarServicos(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id,
            @Parameter(description = "Lista de IDs dos serviços") @RequestBody List<Long> servicosIds) {
        return ResponseEntity.ok(service.adicionarServicos(id, servicosIds));
    }

    @DeleteMapping("/{id}/servicos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Remover serviços da ordem de serviço", description = "Remove serviços de uma ordem de serviço existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviços removidos com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não é possível remover serviços em ordem aguardando aprovação"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<OrdemServicoResponseDTO> removerServicos(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id,
            @Parameter(description = "Lista de IDs dos serviços") @RequestBody List<Long> servicosIds) {
        return ResponseEntity.ok(service.removerServicos(id, servicosIds));
    }

    @PostMapping("/{id}/produtos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Adicionar produtos à ordem de serviço", description = "Adiciona produtos a uma ordem de serviço existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos adicionados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não é possível adicionar produtos em ordem aguardando aprovação"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<List<OsItemDTO>> adicionarProdutos(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id,
            @Parameter(description = "Lista de produtos com quantidade e preço") @RequestBody List<ItemOrdemServicoDTO> produtos) {
        return ResponseEntity.ok(service.adicionarProdutos(id, produtos));
    }

    @DeleteMapping("/{id}/produtos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    @Operation(summary = "Remover produtos da ordem de serviço", description = "Remove produtos de uma ordem de serviço existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos removidos com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não é possível remover produtos em ordem aguardando aprovação"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<OrdemServicoResponseDTO> removerProdutos(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id,
            @Parameter(description = "Lista de IDs dos produtos") @RequestBody List<Long> produtosIds) {
        return ResponseEntity.ok(service.removerProdutos(id, produtosIds));
    }

    @GetMapping("/{id}/orcamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO', 'CLIENTE')")
    @Operation(summary = "Buscar orçamento da ordem de serviço", description = "Busca o orçamento associado a uma ordem de serviço")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orçamento encontrado"),
            @ApiResponse(responseCode = "204", description = "Ordem de serviço não possui orçamento"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    public ResponseEntity<OrcamentoResumoDTO> buscarOrcamento(
            @Parameter(description = "ID da ordem de serviço") @PathVariable Long id) {
        OrdemServicoResponseDTO os = service.buscarPorId(id);
        if (os.getOrcamento() == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(os.getOrcamento());
    }
}
