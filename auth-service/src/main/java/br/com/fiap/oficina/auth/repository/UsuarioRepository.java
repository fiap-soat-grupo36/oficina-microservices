package br.com.fiap.oficina.auth.repository;

import br.com.fiap.oficina.auth.entity.Usuario;
import br.com.fiap.oficina.shared.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByUsernameIgnoreCase(String username);
    List<Usuario> findByRole(Role role);
}
