package br.com.fiap.oficina.workorder.mapper;

import br.com.fiap.oficina.workorder.dto.response.OrdemServicoResponseDTO;
import br.com.fiap.oficina.workorder.dto.response.OrdemServicoResumoDTO;
import br.com.fiap.oficina.workorder.entity.OrdemServico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrdemServicoMapper {

    @Mapping(target = "veiculo", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "mecanico", ignore = true)
    @Mapping(target = "servicos", ignore = true)
    @Mapping(target = "itensOrdemServico", ignore = true)
    @Mapping(target = "orcamento", ignore = true)
    OrdemServicoResponseDTO toDTO(OrdemServico ordemServico);

    @Mapping(target = "veiculoId", source = "veiculoId")
    @Mapping(target = "clienteId", source = "clienteId")
    @Mapping(target = "mecanicoId", source = "mecanicoId")
    @Mapping(target = "orcamentoId", source = "orcamentoId")
    OrdemServicoResumoDTO toResumoDTO(OrdemServico ordemServico);

    List<OrdemServicoResponseDTO> toDTOList(List<OrdemServico> ordens);

    List<OrdemServicoResumoDTO> toResumoDTOList(List<OrdemServico> ordens);
}
