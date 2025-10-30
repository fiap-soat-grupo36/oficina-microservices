package br.com.fiap.oficina.budget.event;

import br.com.fiap.oficina.budget.entity.Orcamento;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrcamentoDisponivelEvent extends ApplicationEvent {
    private final Orcamento orcamento;

    public OrcamentoDisponivelEvent(Object source, Orcamento orcamento) {
        super(source);
        this.orcamento = orcamento;
    }
}
