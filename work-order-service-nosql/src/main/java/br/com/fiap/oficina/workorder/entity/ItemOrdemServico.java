package br.com.fiap.oficina.workorder.entity;

import lombok.*;

import java.math.BigDecimal;

// REMOVIDO: @Entity, @Table, @Id, @GeneratedValue, @ManyToOne
// Agora é um POJO puro (será array embutido em OrdemServico)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemOrdemServico {

    // REMOVIDO: private Long id; (MongoDB não precisa ID para subdocumentos)
    // REMOVIDO: private OrdemServico ordemServico; (não precisa mais da relação bidirecional)

    private Long produtoCatalogoId;
    private Integer quantidade;
    private BigDecimal precoUnitario;
}