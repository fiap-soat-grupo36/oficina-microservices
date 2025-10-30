package br.com.fiap.oficina.auth.service;

import br.com.fiap.oficina.auth.dto.request.UsuarioRequestDTO;
import br.com.fiap.oficina.auth.dto.response.UsuarioResponseDTO;
import br.com.fiap.oficina.auth.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto);

    Optional<UsuarioResponseDTO> buscarPorUsername(String username);

    Optional<Usuario> buscarEntidadePorUsername(String username);

    List<UsuarioResponseDTO> listarTodos();

    UsuarioResponseDTO buscarPorId(Long id);

    UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto);

    void deletar(Long id);
}
