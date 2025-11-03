package br.com.fiap.oficina.workorder.service.impl;

import br.com.fiap.oficina.shared.enums.StatusOrdemServico;
import br.com.fiap.oficina.shared.exception.BusinessException;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.workorder.client.ClienteClient;
import br.com.fiap.oficina.workorder.client.ProdutoCatalogoClient;
import br.com.fiap.oficina.workorder.client.ServicoClient;
import br.com.fiap.oficina.workorder.client.VeiculoClient;
import br.com.fiap.oficina.workorder.dto.request.ItemOrdemServicoDTO;
import br.com.fiap.oficina.workorder.dto.request.OsRequestDTO;
import br.com.fiap.oficina.workorder.dto.response.*;
import br.com.fiap.oficina.workorder.entity.ItemOrdemServico;
import br.com.fiap.oficina.workorder.entity.OrdemServico;
import br.com.fiap.oficina.workorder.event.CalcularOrcamentoEvent;
import br.com.fiap.oficina.workorder.event.VeiculoDisponivelEvent;
import br.com.fiap.oficina.workorder.mapper.OrdemServicoMapper;
import br.com.fiap.oficina.workorder.repository.OrdemServicoRepository;
import br.com.fiap.oficina.workorder.service.OrdemServicoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdemServicoServiceImpl implements OrdemServicoService {

    private final OrdemServicoRepository repository;
    private final OrdemServicoMapper mapper;
    private final ClienteClient clienteClient;
    private final VeiculoClient veiculoClient;
    private final ServicoClient servicoClient;
    private final ProdutoCatalogoClient produtoClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrdemServicoResponseDTO criar(OsRequestDTO request) {
        log.info("Criando ordem de serviço para cliente ID: {} e veículo ID: {}",
                request.getClienteId(), request.getVeiculoId());

        // Valida cliente e veículo
        ClienteResponseDTO cliente = clienteClient.getCliente(request.getClienteId());
        VeiculoResponseDTO veiculo = veiculoClient.getVeiculo(request.getVeiculoId());

        if (cliente == null) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado");
        }
        if (veiculo == null) {
            throw new RecursoNaoEncontradoException("Veículo não encontrado");
        }

        // Cria ordem de serviço
        OrdemServico os = new OrdemServico();
        os.setClienteId(request.getClienteId());
        os.setVeiculoId(request.getVeiculoId());
        os.setObservacoes(request.getObservacoes());
        os.setStatus(StatusOrdemServico.RECEBIDA);
        os.setDataCriacao(LocalDateTime.now());

        // Adiciona serviços
        if (request.getServicosIds() != null && !request.getServicosIds().isEmpty()) {
            for (Long servicoId : request.getServicosIds()) {
                ServicoResponseDTO servico = servicoClient.getServico(servicoId);
                if (servico != null && servico.getAtivo()) {
                    os.addServico(servicoId);
                }
            }
        }

        // Salva a ordem
        os = repository.save(os);

        // Adiciona produtos
        if (request.getProdutos() != null && !request.getProdutos().isEmpty()) {
            for (ItemOrdemServicoDTO produtoDTO : request.getProdutos()) {
                ItemOrdemServico item = new ItemOrdemServico();
                item.setProdutoCatalogoId(produtoDTO.getProdutoCatalogoId());
                item.setQuantidade(produtoDTO.getQuantidade());
                item.setPrecoUnitario(produtoDTO.getPrecoUnitario());
                os.addProduto(item);
            }
            os = repository.save(os);
        }

        // Publica evento para calcular orçamento
        eventPublisher.publishEvent(new CalcularOrcamentoEvent(this, os.getId()));

        return toResponseDTO(os);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarTodos(List<StatusOrdemServico> status) {
        List<OrdemServico> ordens;
        if (status != null && !status.isEmpty()) {
            ordens = repository.findByStatusIn(status);
        } else {
            ordens = repository.findAll();
        }
        return ordens.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrdemServicoResponseDTO buscarPorId(Long id) {
        OrdemServico os = getOrdemServico(id);
        return toResponseDTO(os);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdemServicoResumoDTO> buscarPorCliente(Long clienteId) {
        List<OrdemServico> ordens = repository.findByClienteId(clienteId);
        return mapper.toResumoDTOList(ordens);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdemServicoResumoDTO> buscarPorVeiculo(Long veiculoId) {
        List<OrdemServico> ordens = repository.findByVeiculoId(veiculoId);
        return mapper.toResumoDTOList(ordens);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> buscarPorMecanico(Long mecanicoId) {
        List<OrdemServico> ordens = repository.findByMecanicoId(mecanicoId);
        return ordens.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> buscarAtualizadas() {
        List<OrdemServico> ordens = repository.findOrdensAtualizadas();
        return ordens.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrdemServicoResponseDTO atualizar(Long id, OsRequestDTO request) {
        OrdemServico os = getOrdemServico(id);

        if (request.getObservacoes() != null) {
            os.setObservacoes(request.getObservacoes());
        }

        os = repository.save(os);
        return toResponseDTO(os);
    }

    @Override
    @Transactional
    public void atualizarStatus(Long ordemServicoId, StatusOrdemServico status) {
        OrdemServico os = getOrdemServico(ordemServicoId);
        os.setStatus(status);
        repository.save(os);
    }

    @Override
    @Transactional
    public OrdemServicoResponseDTO atribuirMecanico(Long id, Long mecanicoId) {
        log.info("Atribuindo mecânico ID: {} à ordem de serviço ID: {}", mecanicoId, id);

        OrdemServico os = getOrdemServico(id);

        // Valida se o usuário tem role MECANICO
        // Nota: Em produção, deve-se validar via Auth-Service
        os.setMecanicoId(mecanicoId);
        os = repository.save(os);

        return toResponseDTO(os);
    }

    @Override
    @Transactional
    public OrdemServicoResponseDTO diagnosticar(Long id, String observacoes) {
        log.info("Diagnosticando ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);

        if (observacoes != null) {
            os.setObservacoes(observacoes);
        }

        os = repository.save(os);

        // Publica evento para calcular orçamento
        eventPublisher.publishEvent(new CalcularOrcamentoEvent(this, os.getId()));

        return toResponseDTO(os);
    }

    @Override
    @Transactional
    public OrdemServicoResponseDTO executar(Long id, String observacoes) {
        log.info("Iniciando execução da ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);

        // Valida se ordem de serviço está aguardando aprovação (status correto após orçamento aprovado)
        if (os.getStatus() != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new BusinessException("Ordem de serviço deve estar aguardando aprovação para iniciar execução");
        }

        os.setStatus(StatusOrdemServico.EM_EXECUCAO);
        os.setDataInicioExecucao(LocalDateTime.now());

        if (observacoes != null) {
            os.setObservacoes(observacoes);
        }

        os = repository.save(os);
        return toResponseDTO(os);
    }

    @Override
    @Transactional
    public OrdemServicoResponseDTO finalizar(Long id, String observacoes) {
        log.info("Finalizando ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);

        // Valida se está em execução
        if (os.getStatus() != StatusOrdemServico.EM_EXECUCAO) {
            throw new BusinessException("Ordem de serviço deve estar em execução para ser finalizada");
        }

        os.setStatus(StatusOrdemServico.FINALIZADA);
        os.setDataTerminoExecucao(LocalDateTime.now());

        if (observacoes != null) {
            os.setObservacoes(observacoes);
        }

        os = repository.save(os);

        // Publica evento de veículo disponível
        eventPublisher.publishEvent(new VeiculoDisponivelEvent(this, os.getId(), os.getVeiculoId()));

        return toResponseDTO(os);
    }

    @Override
    @Transactional
    public OrdemServicoResponseDTO entregar(Long id, String observacoes) {
        log.info("Entregando veículo da ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);

        // Valida se está finalizada
        if (os.getStatus() != StatusOrdemServico.FINALIZADA) {
            throw new BusinessException("Ordem de serviço deve estar finalizada para ser entregue");
        }

        os.setStatus(StatusOrdemServico.ENTREGUE);
        os.setDataEntrega(LocalDateTime.now());

        if (observacoes != null) {
            os.setObservacoes(observacoes);
        }

        os = repository.save(os);
        return toResponseDTO(os);
    }

    @Override
    @Transactional
    public List<OsItemDTO> adicionarServicos(Long id, List<Long> servicosIds) {
        log.info("Adicionando serviços à ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);

        if (os.getStatus() == StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new BusinessException("Não é possível adicionar serviços em ordem aguardando aprovação");
        }

        List<OsItemDTO> servicos = new ArrayList<>();
        for (Long servicoId : servicosIds) {
            ServicoResponseDTO servico = servicoClient.getServico(servicoId);
            if (servico != null && servico.getAtivo()) {
                os.addServico(servicoId);
                OsItemDTO item = new OsItemDTO();
                item.setId(servico.getId());
                item.setNome(servico.getNome());
                item.setAtivo(servico.getAtivo());
                item.setDescricao(servico.getDescricao());
                servicos.add(item);
            }
        }

        repository.save(os);
        return servicos;
    }

    @Override
    @Transactional
    public OrdemServicoResponseDTO removerServicos(Long id, List<Long> servicosIds) {
        log.info("Removendo serviços da ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);

        if (os.getStatus() == StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new BusinessException("Não é possível remover serviços em ordem aguardando aprovação");
        }

        for (Long servicoId : servicosIds) {
            os.removeServico(servicoId);
        }

        os = repository.save(os);
        return toResponseDTO(os);
    }

    @Override
    @Transactional
    public List<OsItemDTO> adicionarProdutos(Long id, List<ItemOrdemServicoDTO> produtos) {
        log.info("Adicionando produtos à ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);

        if (os.getStatus() == StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new BusinessException("Não é possível adicionar produtos em ordem aguardando aprovação");
        }

        List<OsItemDTO> itens = new ArrayList<>();
        for (ItemOrdemServicoDTO produtoDTO : produtos) {
            ItemOrdemServico item = new ItemOrdemServico();
            item.setProdutoCatalogoId(produtoDTO.getProdutoCatalogoId());
            item.setQuantidade(produtoDTO.getQuantidade());
            item.setPrecoUnitario(produtoDTO.getPrecoUnitario());
            os.addProduto(item);

            // Busca info do produto para retornar
            try {
                var produto = produtoClient.getProduto(produtoDTO.getProdutoCatalogoId());
                if (produto != null) {
                    OsItemDTO osItem = new OsItemDTO();
                    osItem.setId(produto.getId());
                    osItem.setNome(produto.getNome());
                    osItem.setAtivo(produto.getAtivo());
                    osItem.setDescricao(produto.getDescricao());
                    itens.add(osItem);
                }
            } catch (Exception e) {
                log.warn("Erro ao buscar produto ID: {}", produtoDTO.getProdutoCatalogoId(), e);
            }
        }

        repository.save(os);
        return itens;
    }

    @Override
    @Transactional
    public OrdemServicoResponseDTO removerProdutos(Long id, List<Long> produtosIds) {
        log.info("Removendo produtos da ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);

        if (os.getStatus() == StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new BusinessException("Não é possível remover produtos em ordem aguardando aprovação");
        }

        for (Long produtoId : produtosIds) {
            os.removeProduto(produtoId);
        }

        os = repository.save(os);
        return toResponseDTO(os);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        log.info("Deletando ordem de serviço ID: {}", id);
        OrdemServico os = getOrdemServico(id);
        repository.delete(os);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdemServico getOrdemServico(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));
    }

    @Override
    @Transactional
    public void adicionarOrcamento(Long ordemServicoId, Long orcamentoId, StatusOrdemServico status) {
        log.info("Adicionando orçamento ID: {} à ordem de serviço ID: {}", orcamentoId, ordemServicoId);

        OrdemServico os = getOrdemServico(ordemServicoId);
        os.setOrcamentoId(orcamentoId);
        os.setStatus(status);
        repository.save(os);
    }

    private OrdemServicoResponseDTO toResponseDTO(OrdemServico os) {
        OrdemServicoResponseDTO dto = mapper.toDTO(os);

        // Busca informações complementares
        try {
            if (os.getClienteId() != null) {
                ClienteResponseDTO cliente = clienteClient.getCliente(os.getClienteId());
                if (cliente != null) {
                    ClienteResumoDTO resumo = new ClienteResumoDTO();
                    resumo.setId(cliente.getId());
                    resumo.setNome(cliente.getNome());
                    resumo.setEmail(cliente.getEmail());
                    resumo.setTelefone(cliente.getTelefone());
                    dto.setCliente(resumo);
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao buscar cliente ID: {}", os.getClienteId(), e);
        }

        try {
            if (os.getVeiculoId() != null) {
                VeiculoResponseDTO veiculo = veiculoClient.getVeiculo(os.getVeiculoId());
                if (veiculo != null) {
                    VeiculoResumoDTO resumo = new VeiculoResumoDTO();
                    resumo.setId(veiculo.getId());
                    resumo.setPlaca(veiculo.getPlaca());
                    resumo.setMarca(veiculo.getMarca());
                    resumo.setModelo(veiculo.getModelo());
                    resumo.setAno(veiculo.getAno());
                    dto.setVeiculo(resumo);
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao buscar veículo ID: {}", os.getVeiculoId(), e);
        }

        // Busca serviços
        if (os.getServicosIds() != null && !os.getServicosIds().isEmpty()) {
            List<ServicoResponseDTO> servicos = new ArrayList<>();
            for (Long servicoId : os.getServicosIds()) {
                try {
                    ServicoResponseDTO servico = servicoClient.getServico(servicoId);
                    if (servico != null) {
                        servicos.add(servico);
                    }
                } catch (Exception e) {
                    log.warn("Erro ao buscar serviço ID: {}", servicoId, e);
                }
            }
            dto.setServicos(servicos);
        }

        // Converte itens de ordem de serviço
        if (os.getItensOrdemServico() != null && !os.getItensOrdemServico().isEmpty()) {
            List<ItemOrdemServicoDTO> itens = os.getItensOrdemServico().stream()
                    .map(item -> {
                        ItemOrdemServicoDTO itemDTO = new ItemOrdemServicoDTO();
                        itemDTO.setProdutoCatalogoId(item.getProdutoCatalogoId());
                        itemDTO.setQuantidade(item.getQuantidade());
                        itemDTO.setPrecoUnitario(item.getPrecoUnitario());
                        return itemDTO;
                    })
                    .collect(Collectors.toList());
            dto.setItensOrdemServico(itens);
        }

        return dto;
    }
}
