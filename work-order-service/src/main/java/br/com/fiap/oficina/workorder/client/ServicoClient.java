package br.com.fiap.oficina.workorder.client;

import br.com.fiap.oficina.workorder.dto.response.ServicoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", contextId = "servico-client")
public interface ServicoClient {

    @GetMapping("/api/servicos/{id}")
    ServicoResponseDTO getServico(@PathVariable("id") Long id);
}
