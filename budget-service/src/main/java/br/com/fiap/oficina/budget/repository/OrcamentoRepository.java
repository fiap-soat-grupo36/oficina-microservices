package br.com.fiap.oficina.budget.repository;

import br.com.fiap.oficina.budget.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
}
