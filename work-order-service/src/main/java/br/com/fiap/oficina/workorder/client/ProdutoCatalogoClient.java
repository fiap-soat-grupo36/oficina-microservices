package br.com.fiap.oficina.workorder.client;

import br.com.fiap.oficina.workorder.dto.response.ProdutoCatalogoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "produto-catalog-service", url = "http://localhost:8083")
public interface ProdutoCatalogoClient {

    @GetMapping("/api/catalogo-produtos/{id}")
    ProdutoCatalogoResponseDTO getProduto(@PathVariable("id") Long id);
}
