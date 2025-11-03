package br.com.fiap.oficina.inventory.repository;

import br.com.fiap.oficina.inventory.entity.ReservaEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaEstoqueRepository extends JpaRepository<ReservaEstoque, Long> {

    List<ReservaEstoque> findByOrdemServicoIdAndAtivaTrue(Long ordemServicoId);

    List<ReservaEstoque> findByProdutoCatalogoIdAndAtivaTrue(Long produtoCatalogoId);

    @Query("SELECT r FROM ReservaEstoque r WHERE r.ordemServicoId = :ordemServicoId")
    List<ReservaEstoque> listarReservasProdutosPorOS(@Param("ordemServicoId") Long ordemServicoId);
}
