package br.com.fiap.oficina.customer.controller;

import br.com.fiap.oficina.customer.dto.request.VeiculoRequesDTO;
import br.com.fiap.oficina.customer.dto.response.VeiculoResponseDTO;
import br.com.fiap.oficina.customer.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/veiculos")
@Tag(
        name = "Cadastro e gerenciamento de Veículos",
        description = "Cadastro, busca, atualização e exclusão de veículos."
)
public class VeiculoController {

    private final VeiculoService veiculoService;

    @Autowired
    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar novo veículo",
            description = "Cria um novo veiculo no sistema com base nos dados fornecidos.",
            operationId = "criarVeiculo"
    )
    @ApiResponse(responseCode = "201", description = "Veículo cadastrado com sucesso.", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<VeiculoResponseDTO> criar(@RequestBody @Valid VeiculoRequesDTO dto) {
        VeiculoResponseDTO salvo = veiculoService.salvar(dto);
        URI location = URI.create("/veiculos/" + salvo.getId());
        return ResponseEntity.created(location).body(salvo);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os veículos",
            description = "Retorna uma lista de todas os veículos cadastrados.",
            operationId = "listarTodosVeiculos",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Lista de veículos retornada com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<List<VeiculoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(veiculoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar veículo por ID",
            description = "Retorna os detalhes de um veículo específico com base no ID informado.",
            operationId = "buscarVeiculoPorId",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Veículo encontrado com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<VeiculoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(veiculoService.buscarPorId(id));
    }

    @GetMapping("/placa/{placa}")
    @Operation(
            summary = "Buscar veículo por placa",
            description = "Retorna um veículo correspondente a placa informada (case-insensitive).",
            operationId = "buscarVeiculoPorPlaca",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Veículo encontrado com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<VeiculoResponseDTO> buscarPorPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(veiculoService.buscarPorPlaca(placa));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(
            summary = "Buscar veículos por cliente",
            description = "Retorna todos os veículos de um cliente específico",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Veículos encontrados")
    public ResponseEntity<List<VeiculoResponseDTO>> buscarPorCliente(
            @PathVariable Long clienteId
    ) {
        List<VeiculoResponseDTO> veiculos = veiculoService.buscarPorCliente(clienteId);
        if (veiculos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(veiculos);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar veículo por ID",
            description = "Atualiza os dados de um veículo existente com base no ID informado.",
            operationId = "atualizarVeiculo",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<VeiculoResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid VeiculoRequesDTO dto) {
        return ResponseEntity.ok(veiculoService.atualizar(id, dto));
    }

    @PutMapping("/{id}/transferir")
    @Operation(
            summary = "Transferir veiculo por ID",
            description = "Transfere os dados de um veículo existente com base no ID informado.",
            operationId = "transferirVeiculo",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Veículo transferido com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<VeiculoResponseDTO> transferir(@PathVariable Long id, @RequestParam Long novoClienteId) {
        return ResponseEntity.ok(veiculoService.transferirPropriedade(id, novoClienteId));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar veículo por ID",
            description = "Remove um veículo do sistema com base no ID informado.",
            operationId = "deletarVeiculo",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        veiculoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}