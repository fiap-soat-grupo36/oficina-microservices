package br.com.fiap.oficina.auth.controller;

import br.com.fiap.oficina.auth.dto.request.UsuarioRequestDTO;
import br.com.fiap.oficina.auth.dto.response.UsuarioResponseDTO;
import br.com.fiap.oficina.auth.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Gestão de usuários do sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar novo usuário",
            description = "Cria um novo usuário no sistema com base nos dados fornecidos",
            operationId = "cadastrarUsuario"
    )
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso", content = @Content(mediaType = "application/json"))
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody @Valid UsuarioRequestDTO dto) {
        UsuarioResponseDTO salvo = usuarioService.cadastrar(dto);
        URI location = URI.create("/api/usuarios/" + salvo.getId());
        return ResponseEntity.created(location).body(salvo);
    }

    @GetMapping
    @Operation(
            summary = "Lista todos os usuários",
            description = "Retorna uma lista de todos os usuários cadastrados",
            operationId = "listarUsuarios"
    )
    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso", content = @Content(mediaType = "application/json"))
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos();
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Busca usuário por ID",
            description = "Retorna os detalhes de um usuário específico com base no ID informado",
            operationId = "buscarUsuarioPorId"
    )
    @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso", content = @Content(mediaType = "application/json"))
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza usuário por ID",
            description = "Atualiza os dados de um usuário com base no ID informado",
            operationId = "atualizarUsuario"
    )
    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso", content = @Content(mediaType = "application/json"))
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id,
                                                        @RequestBody @Valid UsuarioRequestDTO dto) {
        UsuarioResponseDTO atualizado = usuarioService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar usuário por ID",
            description = "Remove um usuário do sistema com base no ID informado",
            operationId = "deletarUsuario"
    )
    @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
