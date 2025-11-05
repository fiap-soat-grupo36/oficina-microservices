package br.com.fiap.oficina.auth.security;

import br.com.fiap.oficina.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Set test secret and expiration using reflection
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", "test-secret-key-for-testing-purposes-only-must-be-long-enough-256-bits");
        ReflectionTestUtils.setField(jwtTokenProvider, "expirationInMs", 3600000L);
    }

    @Test
    @DisplayName("Deve gerar token JWT para username")
    void deveGerarTokenJwtParaUsername() {
        // Arrange
        String username = "admin";

        // Act
        String token = jwtTokenProvider.generateToken(username);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Deve gerar token JWT com roles")
    void deveGerarTokenJwtComRoles() {
        // Arrange
        String username = "admin";
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );

        // Act
        String token = jwtTokenProvider.generateToken(username, authorities);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("Deve extrair username do token")
    void deveExtrairUsernameDoToken() {
        // Arrange
        String username = "admin";
        String token = jwtTokenProvider.generateToken(username);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Deve extrair roles do token")
    void deveExtrairRolesDoToken() {
        // Arrange
        String username = "admin";
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );
        String token = jwtTokenProvider.generateToken(username, authorities);

        // Act
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);

        // Assert
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_USER"));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando token não tem roles")
    void deveRetornarListaVaziaQuandoTokenNaoTemRoles() {
        // Arrange
        String username = "admin";
        String token = jwtTokenProvider.generateToken(username);

        // Act
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);

        // Assert
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    @DisplayName("Deve validar token válido")
    void deveValidarTokenValido() {
        // Arrange
        String username = "admin";
        String token = jwtTokenProvider.generateToken(username);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Deve rejeitar token inválido")
    void deveRejeitarTokenInvalido() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve rejeitar token vazio")
    void deveRejeitarTokenVazio() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve rejeitar token null")
    void deveRejeitarTokenNull() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve validar que tokens gerados são válidos")
    void deveValidarQueTokensGeradosSaoValidos() {
        // Arrange
        String username = "admin";

        // Act
        String token1 = jwtTokenProvider.generateToken(username);
        String token2 = jwtTokenProvider.generateToken(username);

        // Assert
        assertTrue(jwtTokenProvider.validateToken(token1));
        assertTrue(jwtTokenProvider.validateToken(token2));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token2));
    }
}
