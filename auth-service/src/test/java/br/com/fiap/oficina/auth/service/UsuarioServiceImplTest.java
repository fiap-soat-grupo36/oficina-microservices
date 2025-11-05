package br.com.fiap.oficina.auth.service;

import br.com.fiap.oficina.auth.dto.request.UsuarioRequestDTO;
import br.com.fiap.oficina.auth.dto.response.UsuarioResponseDTO;
import br.com.fiap.oficina.auth.entity.Usuario;
import br.com.fiap.oficina.auth.mapper.UsuarioMapper;
import br.com.fiap.oficina.auth.repository.UsuarioRepository;
import br.com.fiap.oficina.auth.service.impl.UsuarioServiceImpl;
import br.com.fiap.oficina.shared.enums.Role;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.shared.exception.RoleInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso")
    void deveCadastrarUsuarioComSucesso() {
        // Arrange
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setUsername("joao");
        request.setPassword("senha123");
        request.setNome("João Silva");
        request.setRole(Role.USER);

        Usuario entity = new Usuario();
        entity.setUsername("joao");
        entity.setNome("João Silva");

        Usuario savedEntity = new Usuario();
        savedEntity.setId(1L);
        savedEntity.setUsername("joao");
        savedEntity.setNome("João Silva");

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setUsername("joao");

        when(usuarioMapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode("senha123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(savedEntity);
        when(usuarioMapper.toResponseDTO(savedEntity)).thenReturn(response);

        // Act
        UsuarioResponseDTO result = usuarioService.cadastrar(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("joao", result.getUsername());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(passwordEncoder, times(1)).encode("senha123");
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void deveBuscarUsuarioPorId() {
        // Arrange
        Long id = 1L;
        Usuario entity = new Usuario();
        entity.setId(id);
        entity.setUsername("joao");

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(id);
        response.setUsername("joao");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(entity));
        when(usuarioMapper.toResponseDTO(entity)).thenReturn(response);

        // Act
        UsuarioResponseDTO result = usuarioService.buscarPorId(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("joao", result.getUsername());
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado por ID")
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoPorId() {
        // Arrange
        Long id = 999L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> usuarioService.buscarPorId(id));
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void deveListarTodosOsUsuarios() {
        // Arrange
        Usuario user1 = new Usuario();
        user1.setId(1L);
        user1.setUsername("joao");

        Usuario user2 = new Usuario();
        user2.setId(2L);
        user2.setUsername("maria");

        List<Usuario> usuarios = Arrays.asList(user1, user2);

        UsuarioResponseDTO response1 = new UsuarioResponseDTO();
        response1.setId(1L);
        response1.setUsername("joao");

        UsuarioResponseDTO response2 = new UsuarioResponseDTO();
        response2.setId(2L);
        response2.setUsername("maria");

        when(usuarioRepository.findAll()).thenReturn(usuarios);
        when(usuarioMapper.toResponseDTO(user1)).thenReturn(response1);
        when(usuarioMapper.toResponseDTO(user2)).thenReturn(response2);

        // Act
        List<UsuarioResponseDTO> result = usuarioService.listarTodos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("joao", result.get(0).getUsername());
        assertEquals("maria", result.get(1).getUsername());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar usuário por username")
    void deveBuscarUsuarioPorUsername() {
        // Arrange
        String username = "joao";
        Usuario entity = new Usuario();
        entity.setId(1L);
        entity.setUsername(username);

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setUsername(username);

        when(usuarioRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(entity));
        when(usuarioMapper.toResponseDTO(entity)).thenReturn(response);

        // Act
        UsuarioResponseDTO result = usuarioService.buscarPorUsernameComExcecao(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(usuarioRepository, times(1)).findByUsernameIgnoreCase(username);
    }

    @Test
    @DisplayName("Deve buscar usuários por role")
    void deveBuscarUsuariosPorRole() {
        // Arrange
        Role role = Role.ADMIN;
        Usuario user1 = new Usuario();
        user1.setId(1L);
        user1.setRole(role);

        List<Usuario> usuarios = Arrays.asList(user1);

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(1L);
        response.setRole("ADMIN");

        when(usuarioRepository.findByRole(role)).thenReturn(usuarios);
        when(usuarioMapper.toResponseDTO(user1)).thenReturn(response);

        // Act
        List<UsuarioResponseDTO> result = usuarioService.buscarPorRole("ADMIN");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0).getRole());
        verify(usuarioRepository, times(1)).findByRole(role);
    }

    @Test
    @DisplayName("Deve lançar exceção quando role inválida")
    void deveLancarExcecaoQuandoRoleInvalida() {
        // Act & Assert
        assertThrows(RoleInvalidaException.class, () -> usuarioService.buscarPorRole("INVALID_ROLE"));
        verify(usuarioRepository, never()).findByRole(any());
    }

    @Test
    @DisplayName("Deve atualizar usuário")
    void deveAtualizarUsuario() {
        // Arrange
        Long id = 1L;
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setUsername("joao");
        request.setPassword("novaSenha");
        request.setNome("João Silva Atualizado");
        request.setRole(Role.ADMIN);

        Usuario existingEntity = new Usuario();
        existingEntity.setId(id);
        existingEntity.setUsername("joao");

        Usuario updatedEntity = new Usuario();
        updatedEntity.setId(id);
        updatedEntity.setUsername("joao");
        updatedEntity.setNome("João Silva Atualizado");

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(id);
        response.setNome("João Silva Atualizado");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(passwordEncoder.encode("novaSenha")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(updatedEntity);
        when(usuarioMapper.toResponseDTO(updatedEntity)).thenReturn(response);

        // Act
        UsuarioResponseDTO result = usuarioService.atualizar(id, request);

        // Assert
        assertNotNull(result);
        assertEquals("João Silva Atualizado", result.getNome());
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve deletar usuário (soft delete)")
    void deveDeletarUsuario() {
        // Arrange
        Long id = 1L;
        Usuario entity = new Usuario();
        entity.setId(id);
        entity.setAtivo(true);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(entity));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(entity);

        // Act
        usuarioService.deletar(id);

        // Assert
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }
}
