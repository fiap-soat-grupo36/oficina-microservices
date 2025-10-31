package br.com.fiap.oficina.inventory.entity;

import br.com.fiap.oficina.shared.enums.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "movimentacao_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "produto_catalogo_id", nullable = false)
    private Long produtoCatalogoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimentacao", nullable = false)
    private TipoMovimentacao tipoMovimentacao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "data_movimentacao")
    private LocalDateTime dataMovimentacao = LocalDateTime.now();

    private String observacao;
    
    @Column(name = "numero_nota_fiscal")
    private String numeroNotaFiscal;
    
    private String fornecedor;
    
    @Column(name = "usuario_registro")
    private String usuarioRegistro;
}
