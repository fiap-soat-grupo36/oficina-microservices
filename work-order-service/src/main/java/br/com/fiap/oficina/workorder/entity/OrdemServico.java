package br.com.fiap.oficina.workorder.entity;

import br.com.fiap.oficina.shared.enums.StatusOrdemServico;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "ordem_servico")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrdemServico status = StatusOrdemServico.RECEBIDA;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_inicio_execucao")
    private LocalDateTime dataInicioExecucao;

    @Column(name = "data_termino_execucao")
    private LocalDateTime dataTerminoExecucao;

    @Column(name = "data_entrega")
    private LocalDateTime dataEntrega;

    @Column(length = 1000)
    private String observacoes;

    @Column(name = "veiculo_id")
    private Long veiculoId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "mecanico_id")
    private Long mecanicoId;

    @Column(name = "orcamento_id")
    private Long orcamentoId;

    @ElementCollection
    @CollectionTable(name = "ordem_servico_servicos",
            joinColumns = @JoinColumn(name = "ordem_servico_id"))
    @Column(name = "servico_id")
    private List<Long> servicosIds = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOrdemServico> itensOrdemServico = new ArrayList<>();

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
        produto.setOrdemServico(this);
        itensOrdemServico.add(produto);
    }

    public void removeProduto(Long produtoId) {
        if (itensOrdemServico != null) {
            itensOrdemServico.removeIf(item -> item.getProdutoCatalogoId().equals(produtoId));
        }
    }
}
