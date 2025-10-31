package br.com.fiap.oficina.catalog.repository;

import br.com.fiap.oficina.catalog.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    List<Servico> findByAtivoTrue();

    List<Servico> findByAtivoFalse();

    @Query("SELECT s FROM Servico s WHERE s.ativo = true AND (:termo IS NOT NULL AND :termo <> '' AND (LOWER(s.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(s.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))))")
    List<Servico> buscarPorTermo(@Param("termo") String termo);
}
