package br.com.fiap.oficina.catalog.service.impl;

import br.com.fiap.oficina.catalog.dto.request.ServicoRequestDTO;
import br.com.fiap.oficina.catalog.dto.response.ServicoResponseDTO;
import br.com.fiap.oficina.catalog.entity.Servico;
import br.com.fiap.oficina.catalog.mapper.ServicoMapper;
import br.com.fiap.oficina.catalog.repository.ServicoRepository;
import br.com.fiap.oficina.catalog.service.ServicoService;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoServiceImpl implements ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;

    @Autowired
    public ServicoServiceImpl(ServicoRepository servicoRepository,
                              ServicoMapper servicoMapper) {
        this.servicoRepository = servicoRepository;
        this.servicoMapper = servicoMapper;
    }

    @Override
    public ServicoResponseDTO salvar(ServicoRequestDTO servicoDTO) {
        Servico servico = servicoMapper.toEntity(servicoDTO);
        servico.setAtivo(true);
        Servico salvo = servicoRepository.save(servico);
        return servicoMapper.toDTO(salvo);
    }

    @Override
    public ServicoResponseDTO atualizar(Long id, ServicoRequestDTO servicoDTO) {
        Servico servico = getServico(id);
        servico.setNome(servicoDTO.getNome());
        servico.setDescricao(servicoDTO.getDescricao());
        servico.setCategoria(servicoDTO.getCategoria());
        servico.setPrecoBase(servicoDTO.getPrecoBase());
        servico.setTempoEstimado(servicoMapper.minutesToDuration(servicoDTO.getTempoEstimadoMinutos()));
        Servico atualizado = servicoRepository.save(servico);
        return servicoMapper.toDTO(atualizado);
    }

    @Override
    public ServicoResponseDTO buscarPorId(Long id) {
        return servicoMapper.toDTO(getServico(id));
    }

    @Override
    public List<ServicoResponseDTO> listarTodos() {
        return servicoMapper.toDTO(servicoRepository.findAll());
    }

    @Override
    public List<ServicoResponseDTO> listarAtivos() {
        return servicoMapper.toDTO(servicoRepository.findByAtivoTrue());
    }

    @Override
    public List<ServicoResponseDTO> listarInativos() {
        return servicoMapper.toDTO(servicoRepository.findByAtivoFalse());
    }

    @Override
    public List<ServicoResponseDTO> buscarPorTermo(String termo) {
        return servicoMapper.toDTO(servicoRepository.buscarPorTermo(termo));
    }

    @Override
    public void deletar(Long id) {
        Servico servico = getServico(id);
        servico.setAtivo(false);
        servicoRepository.save(servico);
    }

    @Override
    public Servico getServico(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        String.format("Serviço com ID %d não encontrado", id)));
    }
}
