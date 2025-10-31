package br.com.fiap.oficina.catalog.controller;

import br.com.fiap.oficina.catalog.dto.request.ServicoRequestDTO;
import br.com.fiap.oficina.catalog.dto.response.ServicoResponseDTO;
import br.com.fiap.oficina.catalog.service.ServicoService;
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
@RequestMapping("/api/servicos")
@Tag(
        name = "Serviços",
        description = "Gerenciamento de serviços oferecidos pela oficina."
)
public class ServicoController {

    private final ServicoService servicoService;

    @Autowired
    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar novo serviço.",
            description = "Cria um novo serviço com base nos dados fornecidos.",
            operationId = "criarServico"
    )
    @ApiResponse(responseCode = "201", description = "Serviço cadastrado com sucesso.")
    public ResponseEntity<ServicoResponseDTO> cadastrar(@RequestBody @Valid ServicoRequestDTO servico) {
        ServicoResponseDTO salvo = servicoService.salvar(servico);
        URI location = URI.create("/api/servicos/" + salvo.getId());
        return ResponseEntity.created(location).body(salvo);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os serviços.",
            description = "Retorna uma lista de todos os serviços cadastrados.",
            operationId = "listarServicos"
    )
    @ApiResponse(responseCode = "200", description = "Lista de serviços retornada com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<List<ServicoResponseDTO>> listarTodos() {
        List<ServicoResponseDTO> servicos = servicoService.listarTodos();
        if (servicos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/ativos")
    @Operation(
            summary = "Listar serviços ativos.",
            description = "Retorna uma lista de serviços ativos.",
            operationId = "listarServicosAtivos"
    )
    @ApiResponse(responseCode = "200", description = "Lista de serviços ativos retornada com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<List<ServicoResponseDTO>> listarAtivos() {
        List<ServicoResponseDTO> servicos = servicoService.listarAtivos();
        if (servicos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar serviços por termo.",
            description = "Retorna uma lista de serviços ativos que contêm o termo informado no nome ou descrição (case-insensitive).",
            operationId = "buscarServicosPorTermo"
    )
    @ApiResponse(responseCode = "200", description = "Lista de serviços encontrados com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<List<ServicoResponseDTO>> buscarPorTermo(
            @RequestParam String termo) {
        List<ServicoResponseDTO> servicos = servicoService.buscarPorTermo(termo);
        if (servicos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar serviço por ID.",
            description = "Retorna os detalhes de um serviço específico com base no ID informado.",
            operationId = "buscarServicoPorId"
    )
    @ApiResponse(responseCode = "200", description = "Serviço encontrado com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<ServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        ServicoResponseDTO servico = servicoService.buscarPorId(id);
        return ResponseEntity.ok(servico);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar serviço por ID.",
            description = "Atualiza os dados de um serviço com base no ID informado.",
            operationId = "atualizarServico"
    )
    @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<ServicoResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid ServicoRequestDTO servico) {
        ServicoResponseDTO atualizado = servicoService.atualizar(id, servico);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar serviço por ID.",
            description = "Remove um serviço com base no ID informado (soft delete).",
            operationId = "deletarServico"
    )
    @ApiResponse(responseCode = "204", description = "Serviço deletado com sucesso")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
