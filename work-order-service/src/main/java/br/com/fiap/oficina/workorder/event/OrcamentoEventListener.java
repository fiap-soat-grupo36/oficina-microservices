package br.com.fiap.oficina.workorder.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrcamentoEventListener {

    @Async
    @EventListener
    public void handleCalcularOrcamentoEvent(CalcularOrcamentoEvent event) {
        log.info("Evento CalcularOrcamentoEvent recebido para ordem de serviço ID: {}", event.getOrdemServicoId());
        // Budget-Service escutará este evento e criará o orçamento
    }

    @Async
    @EventListener
    public void handleVeiculoDisponivelEvent(VeiculoDisponivelEvent event) {
        log.info("Evento VeiculoDisponivelEvent recebido para veículo ID: {} da ordem de serviço ID: {}",
                event.getVeiculoId(), event.getOrdemServicoId());
        // Aqui pode-se notificar o cliente que o veículo está disponível
    }
}
