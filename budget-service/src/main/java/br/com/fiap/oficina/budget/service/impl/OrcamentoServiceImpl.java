package br.com.fiap.oficina.budget.service.impl;

import br.com.fiap.oficina.budget.dto.response.OrcamentoResponseDTO;
import br.com.fiap.oficina.budget.entity.Orcamento;
import br.com.fiap.oficina.budget.mapper.OrcamentoMapper;
import br.com.fiap.oficina.budget.repository.OrcamentoRepository;
import br.com.fiap.oficina.budget.service.OrcamentoService;
import br.com.fiap.oficina.shared.enums.StatusOrcamento;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.fiap.oficina.shared.constants.MensagemDeErroConstants.ORCAMENTO_NAO_ENCONTRADO;

@Service
@Slf4j
public class OrcamentoServiceImpl implements OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoMapper orcamentoMapper;

    @Autowired
    public OrcamentoServiceImpl(OrcamentoRepository orcamentoRepository,
                                OrcamentoMapper orcamentoMapper) {
        this.orcamentoRepository = orcamentoRepository;
        this.orcamentoMapper = orcamentoMapper;
    }

    @Override
    @Transactional
    public OrcamentoResponseDTO aprovar(Long id) {
        Orcamento orcamento = getOrcamento(id);

        if (orcamento.getStatusOrcamento() != StatusOrcamento.CRIADO) {
            throw new IllegalStateException("Apenas orçamentos com status CRIADO podem ser aprovados");
        }

        orcamento.setStatusOrcamento(StatusOrcamento.APROVADO);
        orcamento.setDataAprovacao(LocalDateTime.now());

        Orcamento salvo = orcamentoRepository.save(orcamento);
        log.info("Orçamento {} aprovado", id);

        // TODO: Baixa definitiva do estoque (comentado no original)

        return orcamentoMapper.toDTO(salvo);
    }

    @Override
    @Transactional
    public OrcamentoResponseDTO reprovar(Long id) {
        Orcamento orcamento = getOrcamento(id);

        if (orcamento.getStatusOrcamento() != StatusOrcamento.CRIADO) {
            throw new IllegalStateException("Apenas orçamentos com status CRIADO podem ser reprovados");
        }

        orcamento.setStatusOrcamento(StatusOrcamento.REPROVADO);
        orcamento.setDataReprovacao(LocalDateTime.now());

        Orcamento salvo = orcamentoRepository.save(orcamento);
        log.info("Orçamento {} reprovado", id);

        // TODO: Cancelar reservas de estoque via ReservaEstoqueService

        return orcamentoMapper.toDTO(salvo);
    }

    @Override
    public List<OrcamentoResponseDTO> buscarTodos() {
        List<Orcamento> orcamentos = orcamentoRepository.findAll();
        return orcamentoMapper.toDTOList(orcamentos);
    }

    @Override
    public OrcamentoResponseDTO buscarPorId(Long id) {
        Orcamento orcamento = getOrcamento(id);
        return orcamentoMapper.toDTO(orcamento);
    }

    //FIXME: implementar
    @Override
    public OrcamentoResponseDTO buscarPorOrdemServicoId(Long ordemServicoId) {
        return null;
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        Orcamento orcamento = getOrcamento(id);

        if (orcamento.getStatusOrcamento() == StatusOrcamento.APROVADO) {
            throw new br.com.fiap.oficina.shared.exception.OrcamentoJaAprovadoException(
                    "Não é possível deletar um orçamento aprovado");
        }

        orcamentoRepository.delete(orcamento);
        log.info("Orçamento {} deletado com sucesso", id);
    }

    @Override
    public boolean processarResposta(String token, String resposta) {
        // TODO: Implementar lógica de processamento de resposta externa
        log.info("Processando resposta para token: {}, resposta: {}", token, resposta);
        return true;
    }

    private Orcamento getOrcamento(Long id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        String.format(ORCAMENTO_NAO_ENCONTRADO, id)));
    }
}
