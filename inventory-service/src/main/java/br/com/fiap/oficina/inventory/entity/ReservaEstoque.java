package br.com.fiap.oficina.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "reserva_estoque")
public class ReservaEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "produto_catalogo_id", nullable = false)
    private Long produtoCatalogoId;

    @Column(name = "ordem_servico_id", nullable = false)
    private Long ordemServicoId;

    @Column(name = "quantidade_reservada", nullable = false)
    private Integer quantidadeReservada;

    @Column(nullable = false)
    private Boolean ativa = true;

    @Column(name = "data_reserva")
    private LocalDateTime dataReserva = LocalDateTime.now();
}
