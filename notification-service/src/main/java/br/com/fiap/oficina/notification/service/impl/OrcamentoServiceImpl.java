package br.com.fiap.oficina.notification.service.impl;

import br.com.fiap.oficina.notification.model.OrdemServico;
import br.com.fiap.oficina.notification.service.OrcamentoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrcamentoServiceImpl implements OrcamentoService {

    @Override
    public void criar(OrdemServico ordemServico) {
        log.info("Iniciando criação de orçamento para ordem de serviço: {}", ordemServico.getId());
        // TODO: Implementar lógica de criação de orçamento
        // Esta é uma implementação placeholder para o microserviço de notificações
        log.info("Orçamento criado para ordem de serviço: {}", ordemServico.getId());
    }
}
