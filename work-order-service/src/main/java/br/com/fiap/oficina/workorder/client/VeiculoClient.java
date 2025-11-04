package br.com.fiap.oficina.workorder.client;

import br.com.fiap.oficina.workorder.dto.response.VeiculoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", contextId = "veiculo-client")
public interface VeiculoClient {

    @GetMapping("/api/veiculos/{id}")
    VeiculoResponseDTO getVeiculo(@PathVariable("id") Long id);
}
