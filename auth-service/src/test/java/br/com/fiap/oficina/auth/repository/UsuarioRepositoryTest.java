package br.com.fiap.oficina.auth.repository;

import br.com.fiap.oficina.auth.entity.Usuario;
import br.com.fiap.oficina.shared.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve buscar usuário por username")
    void deveBuscarUsuarioPorUsername() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setNome("Administrator");
        usuario.setPassword("$2a$10$password");
        usuario.setRole(Role.ADMIN);
        usuario.setAtivo(true);
        entityManager.persist(usuario);
        entityManager.flush();

        // Act
        Optional<Usuario> result = usuarioRepository.findByUsername("admin");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
        assertEquals(Role.ADMIN, result.get().getRole());
    }

    @Test
    @DisplayName("Deve retornar vazio quando usuário não existe")
    void deveRetornarVazioQuandoUsuarioNaoExiste() {
        // Act
        Optional<Usuario> result = usuarioRepository.findByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Deve buscar usuário por username ignorando case")
    void deveBuscarUsuarioPorUsernameIgnorandoCase() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setUsername("TestUser");
        usuario.setNome("Test User");
        usuario.setPassword("$2a$10$password");
        usuario.setRole(Role.USER);
        usuario.setAtivo(true);
        entityManager.persist(usuario);
        entityManager.flush();

        // Act
        Optional<Usuario> result = usuarioRepository.findByUsernameIgnoreCase("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("TestUser", result.get().getUsername());
    }

    @Test
    @DisplayName("Deve buscar usuários por role")
    void deveBuscarUsuariosPorRole() {
        // Arrange
        Usuario admin1 = new Usuario();
        admin1.setUsername("admin1");
        admin1.setNome("Admin One");
        admin1.setPassword("$2a$10$password");
        admin1.setRole(Role.ADMIN);
        admin1.setAtivo(true);

        Usuario admin2 = new Usuario();
        admin2.setUsername("admin2");
        admin2.setNome("Admin Two");
        admin2.setPassword("$2a$10$password");
        admin2.setRole(Role.ADMIN);
        admin2.setAtivo(true);

        Usuario user = new Usuario();
        user.setUsername("user1");
        user.setNome("Regular User");
        user.setPassword("$2a$10$password");
        user.setRole(Role.USER);
        user.setAtivo(true);

        entityManager.persist(admin1);
        entityManager.persist(admin2);
        entityManager.persist(user);
        entityManager.flush();

        // Act
        List<Usuario> admins = usuarioRepository.findByRole(Role.ADMIN);

        // Assert
        assertNotNull(admins);
        assertEquals(2, admins.size());
        assertTrue(admins.stream().allMatch(u -> u.getRole() == Role.ADMIN));
    }

    @Test
    @DisplayName("Deve buscar usuários com role MECANICO")
    void deveBuscarUsuariosComRoleMecanico() {
        // Arrange
        Usuario mecanico = new Usuario();
        mecanico.setUsername("mecanico1");
        mecanico.setNome("Mechanic One");
        mecanico.setPassword("$2a$10$password");
        mecanico.setRole(Role.MECANICO);
        mecanico.setAtivo(true);
        entityManager.persist(mecanico);
        entityManager.flush();

        // Act
        List<Usuario> mecanicos = usuarioRepository.findByRole(Role.MECANICO);

        // Assert
        assertNotNull(mecanicos);
        assertEquals(1, mecanicos.size());
        assertEquals(Role.MECANICO, mecanicos.get(0).getRole());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há usuários com a role")
    void deveRetornarListaVaziaQuandoNaoHaUsuariosComRole() {
        // Act
        List<Usuario> users = usuarioRepository.findByRole(Role.USER);

        // Assert
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("Deve salvar novo usuário")
    void deveSalvarNovoUsuario() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setUsername("newuser");
        usuario.setNome("New User");
        usuario.setPassword("$2a$10$password");
        usuario.setRole(Role.USER);
        usuario.setAtivo(true);

        // Act
        Usuario saved = usuarioRepository.save(usuario);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("newuser", saved.getUsername());
    }
}
