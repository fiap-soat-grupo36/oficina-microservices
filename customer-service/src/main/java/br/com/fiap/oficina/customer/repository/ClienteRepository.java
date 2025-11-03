package br.com.fiap.oficina.customer.repository;

import br.com.fiap.oficina.customer.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNomeContainingIgnoreCase(String nome);

    Optional<Cliente> findByCpf(String cpf);

    Optional<Cliente> findByEmailIgnoreCase(String email);
}