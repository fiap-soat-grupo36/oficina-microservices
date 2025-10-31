package br.com.fiap.oficina.workorder.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CalcularOrcamentoEvent extends ApplicationEvent {

    private final Long ordemServicoId;

    public CalcularOrcamentoEvent(Object source, Long ordemServicoId) {
        super(source);
        this.ordemServicoId = ordemServicoId;
    }
}
