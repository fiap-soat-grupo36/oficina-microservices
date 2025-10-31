package br.com.fiap.oficina.customer.controller;

import br.com.fiap.oficina.customer.dto.request.ClienteRequestDTO;
import br.com.fiap.oficina.customer.dto.response.ClienteResponseDTO;
import br.com.fiap.oficina.customer.service.ClienteService;
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
@RequestMapping("/api/clientes")
@Tag(
        name = "Cadastro de Cliente",
        description = "Cadastro, consulta, atualização e exclusão de clientes."
)
public class ClienteController {

    private final ClienteService clienteService;

    @Autowired
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar novo cliente.",
            description = "Cria um novo cliente no sistema com base nos dados fornecidos.",
            operationId = "criaCliente"
    )
    @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso.")
    public ResponseEntity<ClienteResponseDTO> cadastrar(@RequestBody @Valid ClienteRequestDTO cliente) {
        ClienteResponseDTO salvo = clienteService.salvar(cliente);
        URI location = URI.create("/clientes" + salvo.getId());
        return ResponseEntity.created(location).body(salvo);
    }

    @GetMapping
    @Operation(
            summary = "Lista todos os cliente.",
            description = "Retorna uma lista de todos os clientes cadastrados.",
            operationId = "listarClientes"
    )
    @ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        List<ClienteResponseDTO> clientes = clienteService.listarClientes();
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busca cliente por ID.",
            description = "Retorna os detalhes de um cliente específico cadastrado com base no ID informado.",
            operationId = "buscarClientePorId"
    )
    @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        ClienteResponseDTO cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar cliente por CPF ou email",
            description = "Retorna cliente específico baseado em CPF ou email informado. Se ambos forem fornecidos, apenas o CPF será utilizado."
    )
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    public ResponseEntity<ClienteResponseDTO> buscarPorCpfOuEmail(
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String email
    ) {
        if (cpf != null && email != null) {
            throw new IllegalArgumentException("Informe apenas CPF ou email, não ambos");
        }
        if (cpf != null) {
            return ResponseEntity.ok(clienteService.buscarPorCpf(cpf));
        }
        if (email != null) {
            return ResponseEntity.ok(clienteService.buscarPorEmail(email));
        }
        throw new IllegalArgumentException("Informe CPF ou email para busca");
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza cliente por ID.",
            description = "Atualiza os dados de um cliente com base no ID informado.",
            operationId = "atualizaClientePorId"
    )
    @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<ClienteResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid ClienteRequestDTO cliente) {
        ClienteResponseDTO clienteAtualizado = clienteService.atualizar(id, cliente);
        return ResponseEntity.ok(clienteAtualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar cliente por ID",
            description = "Remove um cliente do sistema com base no ID informado.",
            operationId = "deletarCliente"
    )
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}