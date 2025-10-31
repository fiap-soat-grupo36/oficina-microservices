package br.com.fiap.oficina.notification.listener;

import br.com.fiap.oficina.notification.event.CalcularOrcamentoEvent;
import br.com.fiap.oficina.notification.event.OrcamentoDisponivelEvent;
import br.com.fiap.oficina.notification.event.VeiculoDisponivelEvent;
import br.com.fiap.oficina.notification.service.EmailService;
import br.com.fiap.oficina.notification.service.OrcamentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoEventListener {
    private final EmailService emailService;
    private final OrcamentoService orcamentoService;

    @EventListener
    public void handleCalcularOrcamento(CalcularOrcamentoEvent event) {
        log.info("Iniciando o cálculo do orçamento para o cliente: {}", event.ordemServico().getCliente().getNome());
        orcamentoService.criar(event.ordemServico());
    }

    @EventListener
    public void handleOrcamentoDisponivel(OrcamentoDisponivelEvent event) {
        final Map<String, Object> variaveis = Map.of(
                "nomeCliente", event.ordemServico().getCliente().getNome(),
                "orcamentoID", event.orcamento().getId(),
                "valorTotal", event.orcamento().getValorTotal(),
                "itensOrcamento", event.orcamento().getItensOrcamento());

        emailService.enviarEmail(
                event.ordemServico().getCliente().getEmail(),
                "Seu orçamento já está disponível",
                "orcamento-disponivel",
                variaveis
        );
    }

    @EventListener
    public void handleVeiculoPronto(VeiculoDisponivelEvent event) {
        Map<String, Object> variaveis = Map.of(
                "nomeCliente", event.clienteResumo().getNome(),
                "veiculoModelo", event.veiculoResumo().getModelo(),
                "placaVeiculo", event.veiculoResumo().getPlaca()
        );

        emailService.enviarEmail(
                event.clienteResumo().getEmail(),
                "Seu veículo está pronto para retirada",
                "veiculo-pronto",
                variaveis
        );
    }
}
