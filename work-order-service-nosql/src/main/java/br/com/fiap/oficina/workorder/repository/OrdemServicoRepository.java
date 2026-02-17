package br.com.fiap.oficina.workorder.repository;

import br.com.fiap.oficina.shared.enums.StatusOrdemServico;
import br.com.fiap.oficina.workorder.entity.OrdemServico;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdemServicoRepository extends MongoRepository<OrdemServico, String> {
    // MUDOU: JpaRepository<OrdemServico, Long> → MongoRepository<OrdemServico, String>

    // Queries automáticas (funcionam igual)
    List<OrdemServico> findByStatusIn(List<StatusOrdemServico> status);
    List<OrdemServico> findByClienteId(Long clienteId);
    List<OrdemServico> findByVeiculoId(Long veiculoId);
    List<OrdemServico> findByMecanicoId(Long mecanicoId);

    // Query MongoDB (substituindo JPQL)
    @Query("{ '$or': [ { 'status': 'AGUARDANDO_APROVACAO' }, { 'orcamento_id': { '$ne': null } } ] }")
    List<OrdemServico> findOrdensAtualizadas();

    // NOVAS QUERIES (campos aninhados - cache)
    @Query("{ 'cliente_cache.nome': { '$regex': ?0, '$options': 'i' } }")
    List<OrdemServico> findByClienteNome(String nome);

    @Query("{ 'veiculo_cache.placa': ?0 }")
    List<OrdemServico> findByVeiculoPlaca(String placa);
}