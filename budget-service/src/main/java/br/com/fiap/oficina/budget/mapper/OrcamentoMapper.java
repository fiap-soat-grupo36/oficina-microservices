package br.com.fiap.oficina.budget.mapper;

import br.com.fiap.oficina.budget.dto.response.OrcamentoResponseDTO;
import br.com.fiap.oficina.budget.dto.response.OrcamentoResumoDTO;
import br.com.fiap.oficina.budget.entity.Orcamento;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrcamentoMapper {
    OrcamentoResponseDTO toDTO(Orcamento orcamento);
    OrcamentoResumoDTO toResumoDTO(Orcamento orcamento);
    List<OrcamentoResponseDTO> toDTOList(List<Orcamento> orcamentos);
}
