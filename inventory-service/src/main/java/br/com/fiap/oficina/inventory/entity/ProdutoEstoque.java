package br.com.fiap.oficina.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "produto_estoque")
public class ProdutoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "produto_catalogo_id", nullable = false, unique = true)
    private Long produtoCatalogoId;

    @Column(name = "quantidade_total", nullable = false)
    private Integer quantidadeTotal = 0;

    @Column(name = "quantidade_reservada", nullable = false)
    private Integer quantidadeReservada = 0;

    @Column(name = "quantidade_disponivel", nullable = false)
    private Integer quantidadeDisponivel = 0;

    @Column(name = "preco_custo_medio", precision = 10, scale = 2)
    private BigDecimal precoCustoMedio = BigDecimal.ZERO;

    @Column(name = "preco_medio_sugerido", precision = 10, scale = 2)
    private BigDecimal precoMedioSugerido = BigDecimal.ZERO;

    @Column(name = "estoque_minimo", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer estoqueMinimo = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
