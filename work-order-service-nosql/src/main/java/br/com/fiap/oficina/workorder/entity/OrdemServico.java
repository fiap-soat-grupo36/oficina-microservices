package br.com.fiap.oficina.workorder.entity;

import br.com.fiap.oficina.shared.enums.StatusOrdemServico;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "ordens_servico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdemServico {

    @Id
    private String id;  // MongoDB usa String (ObjectId)

    @Field("status")
    private StatusOrdemServico status = StatusOrdemServico.RECEBIDA;

    @Field("data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Field("data_inicio_execucao")
    private LocalDateTime dataInicioExecucao;

    @Field("data_termino_execucao")
    private LocalDateTime dataTerminoExecucao;

    @Field("data_entrega")
    private LocalDateTime dataEntrega;

    @Field("observacoes")
    private String observacoes;

    // Referências fracas (IDs)
    @Field("veiculo_id")
    private Long veiculoId;

    @Field("cliente_id")
    private Long clienteId;

    @Field("mecanico_id")
    private Long mecanicoId;

    @Field("orcamento_id")
    private Long orcamentoId;

    // =========================================
    // CACHE LOCAL (NOVO - Sincronizado via eventos)
    // =========================================
    @Field("cliente_cache")
    private ClienteCache clienteCache;

    @Field("veiculo_cache")
    private VeiculoCache veiculoCache;

    @Field("mecanico_cache")
    private MecanicoCache mecanicoCache;

    // Arrays embutidos
    @Field("servicos_ids")
    private List<Long> servicosIds = new ArrayList<>();

    @Field("itens_ordem_servico")
    private List<ItemOrdemServico> itensOrdemServico = new ArrayList<>();

    // =========================================
    // EVENT SOURCING (NOVO - Histórico imutável)
    // =========================================
    @Field("historico_status")
    private List<MudancaStatus> historicoStatus = new ArrayList<>();

    // =========================================
    // MÉTODOS HELPER (mantidos do original)
    // =========================================
    public void addServico(Long servicoId) {
        if (servicosIds == null) {
            servicosIds = new ArrayList<>();
        }
        if (!servicosIds.contains(servicoId)) {
            servicosIds.add(servicoId);
        }
    }

    public void removeServico(Long servicoId) {
        if (servicosIds != null) {
            servicosIds.remove(servicoId);
        }
    }

    public void addProduto(ItemOrdemServico produto) {
        if (itensOrdemServico == null) {
            itensOrdemServico = new ArrayList<>();
        }
        // REMOVIDO: produto.setOrdemServico(this) - não precisa mais
        itensOrdemServico.add(produto);
    }

    public void removeProduto(Long produtoId) {
        if (itensOrdemServico != null) {
            itensOrdemServico.removeIf(item -> item.getProdutoCatalogoId().equals(produtoId));
        }
    }

    // =========================================
    // CLASSES INTERNAS (CACHE)
    // =========================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClienteCache {
        @Field("id")     
        private Long id;
        private String nome;
        private String email;
        private String telefone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VeiculoCache {
        @Field("id")     
        private Long id;
        private String placa;
        private String marca;
        private String modelo;
        private Integer ano;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MecanicoCache {
        @Field("id")     
        private Long id;
        private String nome;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MudancaStatus {
        private StatusOrdemServico statusAnterior;
        private StatusOrdemServico statusNovo;
        private LocalDateTime dataHora;
        private String usuario;
        private String observacao;
    }
}