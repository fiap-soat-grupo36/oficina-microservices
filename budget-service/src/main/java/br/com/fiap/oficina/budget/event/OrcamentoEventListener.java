package br.com.fiap.oficina.budget.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrcamentoEventListener {

    @Async
    @EventListener
    public void handleOrcamentoDisponivel(OrcamentoDisponivelEvent event) {
        log.info("Orçamento disponível para aprovação - ID: {}, Valor: {}", 
                 event.getOrcamento().getId(), 
                 event.getOrcamento().getValorTotal());
        // TODO: Implementar notificação ao cliente (email, SMS, etc)
    }
}
