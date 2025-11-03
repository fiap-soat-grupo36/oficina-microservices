package br.com.fiap.oficina.inventory.repository;

import br.com.fiap.oficina.inventory.entity.MovimentacaoEstoque;
import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByProdutoCatalogoId(Long produtoCatalogoId);

    List<MovimentacaoEstoque> findByTipoMovimentacao(TipoMovimentacao tipo);

    @Query("SELECT m FROM MovimentacaoEstoque m WHERE " +
            "(:produtoCatalogoId IS NULL OR m.produtoCatalogoId = :produtoCatalogoId) AND " +
            "(:tipo IS NULL OR m.tipoMovimentacao = :tipo) AND " +
            "(:dataInicio IS NULL OR m.dataMovimentacao >= :dataInicio) AND " +
            "(:dataFim IS NULL OR m.dataMovimentacao <= :dataFim)")
    List<MovimentacaoEstoque> findWithFilters(
            @Param("produtoCatalogoId") Long produtoCatalogoId,
            @Param("tipo") TipoMovimentacao tipo,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );
}
