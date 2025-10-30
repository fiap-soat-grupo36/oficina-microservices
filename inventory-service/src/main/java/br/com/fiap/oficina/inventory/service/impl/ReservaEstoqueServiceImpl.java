package br.com.fiap.oficina.inventory.service.impl;

import br.com.fiap.oficina.inventory.entity.ProdutoEstoque;
import br.com.fiap.oficina.inventory.entity.ReservaEstoque;
import br.com.fiap.oficina.inventory.repository.ProdutoEstoqueRepository;
import br.com.fiap.oficina.inventory.repository.ReservaEstoqueRepository;
import br.com.fiap.oficina.inventory.service.ReservaEstoqueService;
import br.com.fiap.oficina.shared.exception.EstoqueInsuficienteException;
import br.com.fiap.oficina.shared.exception.QuantidadeInvalidaParaReservaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaEstoqueServiceImpl implements ReservaEstoqueService {

    private final ReservaEstoqueRepository reservaEstoqueRepository;
    private final ProdutoEstoqueRepository produtoEstoqueRepository;

    @Override
    @Transactional
    public ReservaEstoque reservar(Long produtoCatalogoId, Long ordemServicoId, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new QuantidadeInvalidaParaReservaException("Quantidade deve ser maior que zero");
        }

        // Lock pessimista para garantir consistência
        ProdutoEstoque saldo = produtoEstoqueRepository
                .findByProdutoCatalogoIdForUpdate(produtoCatalogoId)
                .orElseGet(() -> {
                    ProdutoEstoque novo = new ProdutoEstoque();
                    novo.setProdutoCatalogoId(produtoCatalogoId);
                    novo.setQuantidadeTotal(0);
                    novo.setQuantidadeReservada(0);
                    novo.setQuantidadeDisponivel(0);
                    return produtoEstoqueRepository.save(novo);
                });

        if (saldo.getQuantidadeDisponivel() < quantidade) {
            throw new EstoqueInsuficienteException(
                    String.format("Estoque insuficiente. Disponível: %d, Solicitado: %d",
                            saldo.getQuantidadeDisponivel(), quantidade)
            );
        }

        // Criar reserva
        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setProdutoCatalogoId(produtoCatalogoId);
        reserva.setOrdemServicoId(ordemServicoId);
        reserva.setQuantidadeReservada(quantidade);
        reserva.setAtiva(true);
        reserva = reservaEstoqueRepository.save(reserva);

        // Atualizar saldo
        saldo.setQuantidadeReservada(saldo.getQuantidadeReservada() + quantidade);
        saldo.setQuantidadeDisponivel(saldo.getQuantidadeTotal() - saldo.getQuantidadeReservada());
        produtoEstoqueRepository.save(saldo);

        return reserva;
    }

    @Override
    @Transactional
    public void cancelarPorOrdemServico(Long ordemServicoId) {
        List<ReservaEstoque> reservas = reservaEstoqueRepository
                .findByOrdemServicoIdAndAtivaTrue(ordemServicoId);

        for (ReservaEstoque reserva : reservas) {
            reserva.setAtiva(false);
            reservaEstoqueRepository.save(reserva);

            // Atualizar saldo com lock pessimista
            ProdutoEstoque saldo = produtoEstoqueRepository
                    .findByProdutoCatalogoIdForUpdate(reserva.getProdutoCatalogoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            saldo.setQuantidadeReservada(saldo.getQuantidadeReservada() - reserva.getQuantidadeReservada());
            saldo.setQuantidadeDisponivel(saldo.getQuantidadeTotal() - saldo.getQuantidadeReservada());
            produtoEstoqueRepository.save(saldo);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaEstoque> listarReservasPorOrdemServico(Long ordemServicoId) {
        return reservaEstoqueRepository.listarReservasProdutosPorOS(ordemServicoId);
    }
}
