package br.com.fiap.oficina.budget.entity;

import br.com.fiap.oficina.shared.enums.StatusOrcamento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orcamento")
public class Orcamento {

    @Id
    @Column(name = "ordem_servico_id")
    private Long id;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOrcamento> itensOrcamento = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status_orcamento", nullable = false)
    private StatusOrcamento statusOrcamento = StatusOrcamento.CRIADO;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Column(name = "data_reprovacao")
    private LocalDateTime dataReprovacao;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column
    private String token;

    public void calcularValorTotal() {
        this.valorTotal = itensOrcamento.stream()
                .map(ItemOrcamento::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(ItemOrcamento item) {
        itensOrcamento.add(item);
        item.setOrcamento(this);
    }
}
