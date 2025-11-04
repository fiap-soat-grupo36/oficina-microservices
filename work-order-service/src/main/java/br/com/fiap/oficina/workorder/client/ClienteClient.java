package br.com.fiap.oficina.workorder.client;

import br.com.fiap.oficina.workorder.dto.response.ClienteResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", contextId = "cliente-client")
public interface ClienteClient {

    @GetMapping("/api/clientes/{id}")
    ClienteResponseDTO getCliente(@PathVariable("id") Long id);
}
