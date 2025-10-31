package br.com.fiap.oficina.customer.service.impl;

import br.com.fiap.oficina.customer.dto.request.VeiculoRequesDTO;
import br.com.fiap.oficina.customer.dto.response.VeiculoResponseDTO;
import br.com.fiap.oficina.customer.entity.Cliente;
import br.com.fiap.oficina.customer.entity.Veiculo;
import br.com.fiap.oficina.customer.mapper.VeiculoMapper;
import br.com.fiap.oficina.customer.repository.VeiculoRepository;
import br.com.fiap.oficina.customer.service.ClienteService;
import br.com.fiap.oficina.customer.service.VeiculoService;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static br.com.fiap.oficina.shared.constants.MensagemDeErroConstants.VEICULO_NAO_ENCONTRADO;
import static br.com.fiap.oficina.shared.constants.MensagemDeErroConstants.VEICULO_PLACA_NAO_ENCONTRADO;

@Service
public class VeiculoServiceImpl implements VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoMapper veiculoMapper;
    private final ClienteService clienteService;

    @Autowired
    public VeiculoServiceImpl(VeiculoRepository veiculoRepository, VeiculoMapper veiculoMapper, ClienteService clienteService) {
        this.veiculoRepository = veiculoRepository;
        this.veiculoMapper = veiculoMapper;
        this.clienteService = clienteService;
    }

    @Override
    public VeiculoResponseDTO salvar(VeiculoRequesDTO dto) {
        // Validar se placa já não está em uso
        if (dto.getPlaca() != null) {
            veiculoRepository.findByPlaca(dto.getPlaca()).ifPresent(v -> {
                throw new IllegalArgumentException("Placa já cadastrada: " + dto.getPlaca());
            });
        }
        
        Veiculo entity = veiculoMapper.toEntity(dto);
        atualizarCliente(dto.getClienteId(), entity);
        Veiculo salvo = veiculoRepository.save(entity);
        return veiculoMapper.toDTO(salvo);
    }

    @Override
    public List<VeiculoResponseDTO> listarTodos() {
        return veiculoMapper.toDTOList(veiculoRepository.findAll());
    }

    @Override
    public VeiculoResponseDTO buscarPorId(Long id) {
        return veiculoMapper.toDTO(getVeiculo(id));
    }

    @Override
    public VeiculoResponseDTO atualizar(Long id, VeiculoRequesDTO request) {
        Veiculo veiculo = getVeiculo(id);

        // Validar se nova placa já não está em uso (se alterada)
        if (request.getPlaca() != null && !request.getPlaca().equals(veiculo.getPlaca())) {
            veiculoRepository.findByPlaca(request.getPlaca()).ifPresent(v -> {
                if (!v.getId().equals(id)) {
                    throw new IllegalArgumentException("Placa já cadastrada: " + request.getPlaca());
                }
            });
        }

        veiculo.setPlaca(request.getPlaca());
        veiculo.setMarca(request.getMarca());
        veiculo.setModelo(request.getModelo());
        veiculo.setAno(request.getAno());
        veiculo.setCor(request.getCor());
        veiculo.setObservacoes(request.getObservacoes());
        
        // Validar se cliente existe (se alterado)
        if (request.getClienteId() != null && !request.getClienteId().equals(veiculo.getCliente().getId())) {
            atualizarCliente(request.getClienteId(), veiculo);
        }

        Veiculo atualizado = veiculoRepository.save(veiculo);
        return veiculoMapper.toDTO(atualizado);
    }

    @Override
    public void deletar(Long id) {
        Veiculo veiculo = getVeiculo(id);

        veiculo.setAtivo(false);
        veiculoRepository.save(veiculo);
    }

    @Override
    public VeiculoResponseDTO buscarPorPlaca(String placa) {
        return veiculoRepository.findByPlaca(placa)
                .map(veiculoMapper::toDTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException(String.format(VEICULO_PLACA_NAO_ENCONTRADO, placa)));
    }
    
    @Override
    public List<VeiculoResponseDTO> buscarPorCliente(Long clienteId) {
        // Valida se cliente existe
        clienteService.getCliente(clienteId);
        
        List<Veiculo> veiculos = veiculoRepository.findByClienteId(clienteId);
        return veiculoMapper.toDTOList(veiculos);
    }

    @Override
    public VeiculoResponseDTO transferirPropriedade(Long id, Long novoClienteId) {
        Veiculo entity = getVeiculo(id);

        atualizarCliente(novoClienteId, entity);
        Veiculo salvo = veiculoRepository.save(entity);
        return veiculoMapper.toDTO(salvo);
    }

    @Override
    public Veiculo getVeiculo(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(String.format(VEICULO_NAO_ENCONTRADO, id)));
    }

    private void atualizarCliente(Long id, Veiculo veiculo) {
        Cliente cliente = clienteService.getCliente(id);
        veiculo.setCliente(cliente);
    }
}