package br.com.fiap.oficina.auth.controller;

import br.com.fiap.oficina.shared.dto.request.AuthRequestDTO;
import br.com.fiap.oficina.shared.dto.response.AuthResponseDTO;
import br.com.fiap.oficina.shared.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação de usuários")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuário",
            description = "Realiza a autenticação do usuário e retorna um token JWT",
            operationId = "authenticate"
    )
    @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso", content = @Content(mediaType = "application/json"))
    public ResponseEntity<AuthResponseDTO> authenticate(@RequestBody @Valid AuthRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication.getName(), authentication.getAuthorities());
        return ResponseEntity.ok(new AuthResponseDTO(token));
    }
}
