package br.com.fiap.oficina.catalog.entity;

import br.com.fiap.oficina.shared.converter.DurationToLongConverter;
import br.com.fiap.oficina.shared.enums.CategoriaServico;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Duration;

@Entity
@Getter
@Setter
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaServico categoria;

    @Column(name = "preco_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoBase;

    @Convert(converter = DurationToLongConverter.class)
    @Column(name = "tempo_estimado")
    private Duration tempoEstimado;

    @Column(nullable = false)
    private Boolean ativo = true;
}
