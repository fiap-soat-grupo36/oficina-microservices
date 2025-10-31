package br.com.fiap.oficina.workorder.repository;

import br.com.fiap.oficina.shared.enums.StatusOrdemServico;
import br.com.fiap.oficina.workorder.entity.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    List<OrdemServico> findByStatusIn(List<StatusOrdemServico> status);

    List<OrdemServico> findByClienteId(Long clienteId);

    List<OrdemServico> findByVeiculoId(Long veiculoId);

    List<OrdemServico> findByMecanicoId(Long mecanicoId);

    @Query("SELECT os FROM OrdemServico os WHERE os.status = 'AGUARDANDO_APROVACAO' OR os.orcamentoId IS NOT NULL")
    List<OrdemServico> findOrdensAtualizadas();
}
