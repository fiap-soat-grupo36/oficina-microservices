package br.com.fiap.oficina.notification.event;

import br.com.fiap.oficina.notification.model.Orcamento;
import br.com.fiap.oficina.notification.model.OrdemServico;

public record OrcamentoDisponivelEvent(OrdemServico ordemServico, Orcamento orcamento) {
}
