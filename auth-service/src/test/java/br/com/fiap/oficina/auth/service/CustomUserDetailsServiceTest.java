package br.com.fiap.oficina.auth.service;

import br.com.fiap.oficina.auth.entity.Usuario;
import br.com.fiap.oficina.shared.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Deve carregar usuário por username com sucesso")
    void deveCarregarUsuarioPorUsername() {
        // Arrange
        String username = "admin";
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(username);
        usuario.setPassword("$2a$10$encodedPassword");
        usuario.setNome("Administrator");
        usuario.setRole(Role.ADMIN);

        when(usuarioService.buscarEntidadePorUsername(username)).thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        verify(usuarioService, times(1)).buscarEntidadePorUsername(username);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // Arrange
        String username = "nonexistent";
        when(usuarioService.buscarEntidadePorUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, 
            () -> customUserDetailsService.loadUserByUsername(username));
        verify(usuarioService, times(1)).buscarEntidadePorUsername(username);
    }

    @Test
    @DisplayName("Deve carregar usuário com role USER")
    void deveCarregarUsuarioComRoleUser() {
        // Arrange
        String username = "user";
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setUsername(username);
        usuario.setPassword("$2a$10$userPassword");
        usuario.setNome("Regular User");
        usuario.setRole(Role.USER);

        when(usuarioService.buscarEntidadePorUsername(username)).thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        verify(usuarioService, times(1)).buscarEntidadePorUsername(username);
    }

    @Test
    @DisplayName("Deve carregar usuário com role MECANICO")
    void deveCarregarUsuarioComRoleMecanico() {
        // Arrange
        String username = "mecanico";
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setUsername(username);
        usuario.setPassword("$2a$10$mecanicoPassword");
        usuario.setNome("Mechanic User");
        usuario.setRole(Role.MECANICO);

        when(usuarioService.buscarEntidadePorUsername(username)).thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MECANICO")));
        verify(usuarioService, times(1)).buscarEntidadePorUsername(username);
    }
}
