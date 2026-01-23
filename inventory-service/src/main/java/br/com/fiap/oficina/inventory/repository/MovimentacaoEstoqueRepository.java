package br.com.fiap.oficina.inventory.repository;

import br.com.fiap.oficina.inventory.entity.MovimentacaoEstoque;
import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByProdutoCatalogoId(Long produtoCatalogoId);

    List<MovimentacaoEstoque> findByTipoMovimentacao(TipoMovimentacao tipo);

    // Métodos derivados - Spring Data gera a query automaticamente
    List<MovimentacaoEstoque> findByDataMovimentacaoBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    List<MovimentacaoEstoque> findByTipoMovimentacaoAndDataMovimentacaoBetween(
            TipoMovimentacao tipo, LocalDateTime dataInicio, LocalDateTime dataFim);

    List<MovimentacaoEstoque> findByProdutoCatalogoIdAndDataMovimentacaoBetween(
            Long produtoCatalogoId, LocalDateTime dataInicio, LocalDateTime dataFim);

    List<MovimentacaoEstoque> findByProdutoCatalogoIdAndTipoMovimentacao(
            Long produtoCatalogoId, TipoMovimentacao tipo);

    List<MovimentacaoEstoque> findByProdutoCatalogoIdAndTipoMovimentacaoAndDataMovimentacaoBetween(
            Long produtoCatalogoId, TipoMovimentacao tipo, LocalDateTime dataInicio, LocalDateTime dataFim);
}
