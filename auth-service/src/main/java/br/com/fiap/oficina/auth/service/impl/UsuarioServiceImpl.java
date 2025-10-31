package br.com.fiap.oficina.auth.service.impl;

import br.com.fiap.oficina.auth.dto.request.UsuarioRequestDTO;
import br.com.fiap.oficina.auth.dto.response.UsuarioResponseDTO;
import br.com.fiap.oficina.auth.entity.Usuario;
import br.com.fiap.oficina.auth.mapper.UsuarioMapper;
import br.com.fiap.oficina.auth.repository.UsuarioRepository;
import br.com.fiap.oficina.auth.service.UsuarioService;
import br.com.fiap.oficina.shared.enums.Role;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.shared.exception.RoleInvalidaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              UsuarioMapper usuarioMapper,
                              @Lazy PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto) {
        Usuario usuario = usuarioMapper.toEntity(dto);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setAtivo(true);
        Usuario salvo = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(salvo);
    }

    @Override
    public Optional<UsuarioResponseDTO> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .map(usuarioMapper::toResponseDTO);
    }

    @Override
    public Optional<Usuario> buscarEntidadePorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + id));
        return usuarioMapper.toResponseDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + id));

        usuario.setUsername(dto.getUsername());
        usuario.setNome(dto.getNome());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRole(dto.getRole());

        Usuario atualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(atualizado);
    }

    @Override
    public void deletar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + id));

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Override
    public UsuarioResponseDTO buscarPorUsernameComExcecao(String username) {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado com username: " + username
                ));

        return usuarioMapper.toResponseDTO(usuario);
    }

    @Override
    public List<UsuarioResponseDTO> buscarPorRole(String role) {
        // Validar se role existe no enum
        Role roleEnum;
        try {
            roleEnum = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RoleInvalidaException("Role inválida: " + role);
        }

        List<Usuario> usuarios = usuarioRepository.findByRole(roleEnum);
        return usuarios.stream()
                .map(usuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
