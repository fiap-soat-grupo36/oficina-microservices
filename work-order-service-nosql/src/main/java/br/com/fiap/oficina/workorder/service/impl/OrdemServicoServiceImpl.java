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
// REMOVIDO: import org.springframework.transaction.annotation.Transactional;
// MongoDB não usa @Transactional da mesma forma (sem JPA)

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
    public OrdemServicoResponseDTO criar(OsRequestDTO request) {
        log.info("Criando ordem de serviço para cliente ID: {} e veículo ID: {}",
                request.getClienteId(), request.getVeiculoId());

        // Valida e busca dados do cliente
        ClienteResponseDTO cliente = clienteClient.getCliente(request.getClienteId());
        if (cliente == null) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado");
        }

        // Valida e busca dados do veículo
        VeiculoResponseDTO veiculo = veiculoClient.getVeiculo(request.getVeiculoId());
        if (veiculo == null) {
            throw new RecursoNaoEncontradoException("Veículo não encontrado");
        }

        // =========================================
        // CRIA ORDEM DE SERVIÇO COM CACHE LOCAL
        // =========================================
        OrdemServico os = OrdemServico.builder()
                .status(StatusOrdemServico.RECEBIDA)
                .dataCriacao(LocalDateTime.now())
                .clienteId(request.getClienteId())
                .veiculoId(request.getVeiculoId())
                .observacoes(request.getObservacoes())
                // ===== CACHE DO CLIENTE (desnormalizado) =====
                .clienteCache(OrdemServico.ClienteCache.builder()
                        .id(cliente.getId())
                        .nome(cliente.getNome())
                        .email(cliente.getEmail())
                        .telefone(cliente.getTelefone())
                        .build())
                // ===== CACHE DO VEÍCULO (desnormalizado) =====
                .veiculoCache(OrdemServico.VeiculoCache.builder()
                        .id(veiculo.getId())
                        .placa(veiculo.getPlaca())
                        .marca(veiculo.getMarca())
                        .modelo(veiculo.getModelo())
                        .ano(veiculo.getAno())
                        .build())
                .servicosIds(new ArrayList<>())
                .itensOrdemServico(new ArrayList<>())
                .historicoStatus(new ArrayList<>())
                .build();

        // =========================================
        // EVENT SOURCING: Registra criação inicial
        // =========================================
        registrarMudancaStatus(os, null, StatusOrdemServico.RECEBIDA, "Sistema", "OS criada no sistema");

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
                ItemOrdemServico item = ItemOrdemServico.builder()
                        .produtoCatalogoId(produtoDTO.getProdutoCatalogoId())
                        .quantidade(produtoDTO.getQuantidade())
                        .precoUnitario(produtoDTO.getPrecoUnitario())
                        .build();
                os.addProduto(item);
            }
            os = repository.save(os);
        }

        // Publica evento para calcular orçamento
        eventPublisher.publishEvent(new CalcularOrcamentoEvent(this, os.getId()));

        return toResponseDTO(os);
    }

    @Override
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
    public OrdemServicoResponseDTO buscarPorId(Long id) {
        // IMPORTANTE: MongoDB usa String (ObjectId), não Long
        // Por compatibilidade, convertemos aqui
        String mongoId = String.valueOf(id);
        OrdemServico os = repository.findById(mongoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));
        return toResponseDTO(os);
    }

    @Override
    public List<OrdemServicoResumoDTO> buscarPorCliente(Long clienteId) {
        List<OrdemServico> ordens = repository.findByClienteId(clienteId);
        return mapper.toResumoDTOList(ordens);
    }

    @Override
    public List<OrdemServicoResumoDTO> buscarPorVeiculo(Long veiculoId) {
        List<OrdemServico> ordens = repository.findByVeiculoId(veiculoId);
        return mapper.toResumoDTOList(ordens);
    }

    @Override
    public List<OrdemServicoResponseDTO> buscarPorMecanico(Long mecanicoId) {
        List<OrdemServico> ordens = repository.findByMecanicoId(mecanicoId);
        return ordens.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrdemServicoResponseDTO> buscarAtualizadas() {
        List<OrdemServico> ordens = repository.findOrdensAtualizadas();
        return ordens.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrdemServicoResponseDTO atualizar(Long id, OsRequestDTO request) {
        OrdemServico os = getOrdemServico(id);

        if (request.getObservacoes() != null) {
            os.setObservacoes(request.getObservacoes());
        }

        os = repository.save(os);
        return toResponseDTO(os);
    }

    @Override
    public void atualizarStatus(Long ordemServicoId, StatusOrdemServico status) {
        OrdemServico os = getOrdemServico(ordemServicoId);
        
        StatusOrdemServico statusAnterior = os.getStatus();
        os.setStatus(status);
        
        // ===== EVENT SOURCING =====
        registrarMudancaStatus(os, statusAnterior, status, "Sistema", 
                "Mudança de status via atualização externa");
        
        repository.save(os);
    }

    @Override
    public OrdemServicoResponseDTO atribuirMecanico(Long id, Long mecanicoId) {
        log.info("Atribuindo mecânico ID: {} à ordem de serviço ID: {}", mecanicoId, id);

        OrdemServico os = getOrdemServico(id);

        // TODO: Buscar dados do mecânico via Auth-Service para popular cache
        // MecanicoDTO mecanico = authClient.getMecanico(mecanicoId);
        
        os.setMecanicoId(mecanicoId);
        
        // TODO: Popular cache do mecânico quando implementar Feign
        // os.setMecanicoCache(OrdemServico.MecanicoCache.builder()
        //         .id(mecanico.getId())
        //         .nome(mecanico.getNome())
        //         .build());
        
        os = repository.save(os);

        return toResponseDTO(os);
    }

    @Override
    public OrdemServicoResponseDTO diagnosticar(Long id, String observacoes) {
        log.info("Diagnosticando ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);
        
        StatusOrdemServico statusAnterior = os.getStatus();
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);

        if (observacoes != null) {
            os.setObservacoes(observacoes);
        }

        // ===== EVENT SOURCING =====
        registrarMudancaStatus(os, statusAnterior, StatusOrdemServico.EM_DIAGNOSTICO, 
                "Mecânico", observacoes != null ? observacoes : "Iniciando diagnóstico");

        os = repository.save(os);

        // Publica evento para calcular orçamento
        eventPublisher.publishEvent(new CalcularOrcamentoEvent(this, os.getId()));

        return toResponseDTO(os);
    }

    @Override
    public OrdemServicoResponseDTO executar(Long id, String observacoes) {
        log.info("Executando ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);

        if (os.getStatus() != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new BusinessException("Ordem de serviço precisa estar AGUARDANDO_APROVACAO");
        }

        StatusOrdemServico statusAnterior = os.getStatus();
        os.setStatus(StatusOrdemServico.EM_EXECUCAO);
        os.setDataInicioExecucao(LocalDateTime.now());

        if (observacoes != null) {
            os.setObservacoes(observacoes);
        }

        // ===== EVENT SOURCING =====
        registrarMudancaStatus(os, statusAnterior, StatusOrdemServico.EM_EXECUCAO, 
                "Mecânico", observacoes != null ? observacoes : "Iniciando execução dos serviços");

        os = repository.save(os);
        return toResponseDTO(os);
    }

    @Override
    public OrdemServicoResponseDTO finalizar(Long id, String observacoes) {
        log.info("Finalizando ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);

        if (os.getStatus() != StatusOrdemServico.EM_EXECUCAO) {
            throw new BusinessException("Ordem de serviço precisa estar EM_EXECUCAO");
        }

        StatusOrdemServico statusAnterior = os.getStatus();
        os.setStatus(StatusOrdemServico.FINALIZADA);
        os.setDataTerminoExecucao(LocalDateTime.now());

        if (observacoes != null) {
            os.setObservacoes(observacoes);
        }

        // ===== EVENT SOURCING =====
        registrarMudancaStatus(os, statusAnterior, StatusOrdemServico.FINALIZADA, 
                "Mecânico", observacoes != null ? observacoes : "Serviço concluído com sucesso");

        os = repository.save(os);

        // Publica evento de veículo disponível para entrega
        eventPublisher.publishEvent(new VeiculoDisponivelEvent(this, os.getVeiculoId()));

        return toResponseDTO(os);
    }

    @Override
    public OrdemServicoResponseDTO entregar(Long id, String observacoes) {
        log.info("Entregando veículo da ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);

        if (os.getStatus() != StatusOrdemServico.FINALIZADA) {
            throw new BusinessException("Ordem de serviço precisa estar FINALIZADA");
        }

        StatusOrdemServico statusAnterior = os.getStatus();
        os.setStatus(StatusOrdemServico.ENTREGUE);
        os.setDataEntrega(LocalDateTime.now());

        if (observacoes != null) {
            os.setObservacoes(observacoes);
        }

        // ===== EVENT SOURCING =====
        registrarMudancaStatus(os, statusAnterior, StatusOrdemServico.ENTREGUE, 
                "Atendente", observacoes != null ? observacoes : "Veículo entregue ao cliente");

        os = repository.save(os);
        return toResponseDTO(os);
    }

    @Override
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
    public List<OsItemDTO> adicionarProdutos(Long id, List<ItemOrdemServicoDTO> produtos) {
        log.info("Adicionando produtos à ordem de serviço ID: {}", id);

        OrdemServico os = getOrdemServico(id);

        if (os.getStatus() == StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new BusinessException("Não é possível adicionar produtos em ordem aguardando aprovação");
        }

        List<OsItemDTO> itens = new ArrayList<>();
        for (ItemOrdemServicoDTO produtoDTO : produtos) {
            ItemOrdemServico item = ItemOrdemServico.builder()
                    .produtoCatalogoId(produtoDTO.getProdutoCatalogoId())
                    .quantidade(produtoDTO.getQuantidade())
                    .precoUnitario(produtoDTO.getPrecoUnitario())
                    .build();
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
    public void deletar(Long id) {
        log.info("Deletando ordem de serviço ID: {}", id);
        OrdemServico os = getOrdemServico(id);
        repository.delete(os);
    }

    @Override
    public OrdemServico getOrdemServico(Long id) {
        // IMPORTANTE: MongoDB usa String (ObjectId), não Long
        String mongoId = String.valueOf(id);
        return repository.findById(mongoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));
    }

    @Override
    public void adicionarOrcamento(Long ordemServicoId, Long orcamentoId, StatusOrdemServico status) {
        log.info("Adicionando orçamento ID: {} à ordem de serviço ID: {}", orcamentoId, ordemServicoId);

        OrdemServico os = getOrdemServico(ordemServicoId);
        
        StatusOrdemServico statusAnterior = os.getStatus();
        os.setOrcamentoId(orcamentoId);
        os.setStatus(status);
        
        // ===== EVENT SOURCING =====
        registrarMudancaStatus(os, statusAnterior, status, "Sistema", 
                "Orçamento criado e associado à OS");
        
        repository.save(os);
    }

    // =========================================
    // MÉTODOS PRIVADOS (HELPERS)
    // =========================================

    /**
     * Registra mudança de status no histórico (Event Sourcing)
     */
    private void registrarMudancaStatus(OrdemServico os, 
                                       StatusOrdemServico statusAnterior,
                                       StatusOrdemServico statusNovo,
                                       String usuario,
                                       String observacao) {
        OrdemServico.MudancaStatus mudanca = OrdemServico.MudancaStatus.builder()
                .statusAnterior(statusAnterior)
                .statusNovo(statusNovo)
                .dataHora(LocalDateTime.now())
                .usuario(usuario)
                .observacao(observacao)
                .build();
        
        if (os.getHistoricoStatus() == null) {
            os.setHistoricoStatus(new ArrayList<>());
        }
        os.getHistoricoStatus().add(mudanca);
        
        log.debug("Registrada mudança de status: {} → {} para OS ID: {}", 
                statusAnterior, statusNovo, os.getId());
    }

    /**
     * Converte OrdemServico para DTO com dados enriquecidos
     * 
     * IMPORTANTE: Agora usa os dados do CACHE LOCAL (cliente_cache, veiculo_cache)
     * em vez de buscar via Feign, reduzindo latência de rede
     */
    private OrdemServicoResponseDTO toResponseDTO(OrdemServico os) {
        OrdemServicoResponseDTO dto = mapper.toDTO(os);

        // =========================================
        // USA CACHE LOCAL (não busca via Feign)
        // =========================================
        if (os.getClienteCache() != null) {
            ClienteResumoDTO resumo = new ClienteResumoDTO();
            resumo.setId(os.getClienteCache().getId());
            resumo.setNome(os.getClienteCache().getNome());
            resumo.setEmail(os.getClienteCache().getEmail());
            resumo.setTelefone(os.getClienteCache().getTelefone());
            dto.setCliente(resumo);
        } else {
            // Fallback: busca via Feign se cache não existir (OSs antigas)
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
        }

        // =========================================
        // USA CACHE LOCAL (veículo)
        // =========================================
        if (os.getVeiculoCache() != null) {
            VeiculoResumoDTO resumo = new VeiculoResumoDTO();
            resumo.setId(os.getVeiculoCache().getId());
            resumo.setPlaca(os.getVeiculoCache().getPlaca());
            resumo.setMarca(os.getVeiculoCache().getMarca());
            resumo.setModelo(os.getVeiculoCache().getModelo());
            resumo.setAno(os.getVeiculoCache().getAno());
            dto.setVeiculo(resumo);
        } else {
            // Fallback: busca via Feign se cache não existir (OSs antigas)
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
        }

        // =========================================
        // BUSCA SERVIÇOS (ainda via Feign - não são cacheados localmente)
        // =========================================
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

        // =========================================
        // CONVERTE ITENS (produtos) - já estão embutidos no documento
        // =========================================
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