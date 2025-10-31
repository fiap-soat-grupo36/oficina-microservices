package br.com.fiap.oficina.catalog.repository;

import br.com.fiap.oficina.catalog.entity.ProdutoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoCatalogoRepository extends JpaRepository<ProdutoCatalogo, Long> {

    List<ProdutoCatalogo> findByAtivoTrue();

    List<ProdutoCatalogo> findByAtivoFalse();

    @Query("SELECT p FROM ProdutoCatalogo p WHERE p.ativo = true AND (LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%')))")
    List<ProdutoCatalogo> buscarPorTermo(@Param("termo") String termo);
}
