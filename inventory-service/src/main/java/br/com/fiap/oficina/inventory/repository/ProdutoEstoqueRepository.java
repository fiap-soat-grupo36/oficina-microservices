package br.com.fiap.oficina.inventory.repository;

import br.com.fiap.oficina.inventory.entity.ProdutoEstoque;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutoEstoqueRepository extends JpaRepository<ProdutoEstoque, Long> {

    Optional<ProdutoEstoque> findByProdutoCatalogoId(Long produtoCatalogoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProdutoEstoque p WHERE p.produtoCatalogoId = :produtoCatalogoId")
    Optional<ProdutoEstoque> findByProdutoCatalogoIdForUpdate(@Param("produtoCatalogoId") Long produtoCatalogoId);
}
