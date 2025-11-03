package br.com.fiap.oficina.inventory.service;

import br.com.fiap.oficina.inventory.dto.request.ReservaLoteRequestDTO;
import br.com.fiap.oficina.inventory.dto.response.ReservaLoteResponseDTO;

public interface ReservaEstoqueLoteService {

    ReservaLoteResponseDTO reservarEmLote(ReservaLoteRequestDTO request);
}
