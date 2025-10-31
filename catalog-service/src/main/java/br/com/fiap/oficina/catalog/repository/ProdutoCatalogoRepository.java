package br.com.fiap.oficina.catalog.repository;

import br.com.fiap.oficina.catalog.entity.ProdutoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoCatalogoRepository extends JpaRepository<ProdutoCatalogo, Long> {

    List<ProdutoCatalogo> findByAtivoTrue();

    List<ProdutoCatalogo> findByAtivoFalse();
}
