package br.com.fiap.oficina.customer.service.impl;

import br.com.fiap.oficina.customer.dto.request.ClienteRequestDTO;
import br.com.fiap.oficina.customer.dto.response.ClienteResponseDTO;
import br.com.fiap.oficina.customer.entity.Cliente;
import br.com.fiap.oficina.customer.mapper.ClienteMapper;
import br.com.fiap.oficina.customer.repository.ClienteRepository;
import br.com.fiap.oficina.customer.service.ClienteService;
import br.com.fiap.oficina.shared.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.shared.utils.ClienteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static br.com.fiap.oficina.shared.constants.MensagemDeErroConstants.CLIENTE_NAO_ENCONTRADO;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    private final ClienteMapper clienteMapper;

    @Autowired
    public ClienteServiceImpl(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Override
    public ClienteResponseDTO salvar(ClienteRequestDTO cliente) {
        ClienteValidator.validarCliente(cliente);
        Cliente entity = clienteMapper.toEntity(cliente);

        if (entity.getCpf() != null && !entity.getCpf().isBlank()) {
            String cpfFormatado = ClienteValidator.formatarCpf(entity.getCpf());
            entity.setCpf(cpfFormatado);

        } else if (entity.getCnpj() != null && !entity.getCnpj().isBlank()) {
            String cnpjFormatado = ClienteValidator.formatarCnpj(entity.getCnpj());
            entity.setCnpj(cnpjFormatado);
        }

        Cliente salvo = clienteRepository.save(entity);
        return clienteMapper.toDTO(salvo);
    }

    @Override
    public ClienteResponseDTO buscarPorId(Long id) {
        return clienteMapper.toDTO(getCliente(id));
    }

    @Override
    public List<ClienteResponseDTO> buscarPorNome(String nome) {
        List<Cliente> clientes = clienteRepository.findByNomeContainingIgnoreCase(nome.toLowerCase());
        return clientes.stream()
                .map(clienteMapper::toDTO)
                .toList();

    }

    @Override
    public List<ClienteResponseDTO> listarClientes() {
        return clienteMapper.toDTO(clienteRepository.findAll());
    }

    @Override
    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO request) {
        Cliente cliente = getCliente(id);

        cliente.setEmail(request.getEmail());
        cliente.setNome(request.getNome());
        cliente.setTelefone(request.getTelefone());
        cliente.setEndereco(request.getEndereco());
        cliente.setObservacao(request.getObservacao());
        cliente.setDataNascimento(request.getDataNascimento());

        Cliente atualizado = clienteRepository.save(cliente);
        return clienteMapper.toDTO(atualizado);
    }

    @Override
    public void deletar(Long id) {
        Cliente cliente = getCliente(id);

        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    @Override
    public ClienteResponseDTO buscarPorCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF não pode ser vazio");
        }

        String cpfLimpo = cpf.replaceAll("[^\\d]", "");

        if (!ClienteValidator.cpfValido(cpfLimpo)) {
            throw new IllegalArgumentException("CPF inválido: " + cpf);
        }

        String cpfFormatado = ClienteValidator.formatarCpf(cpfLimpo);

        Cliente cliente = clienteRepository.findByCpf(cpfFormatado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com CPF: " + cpf));

        return clienteMapper.toDTO(cliente);
    }

    @Override
    public ClienteResponseDTO buscarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email não pode ser vazio");
        }

        if (!ClienteValidator.emailValido(email)) {
            throw new IllegalArgumentException("Email inválido: " + email);
        }

        Cliente cliente = clienteRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com email: " + email));

        return clienteMapper.toDTO(cliente);
    }

    @Override
    public Cliente getCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(String.format(CLIENTE_NAO_ENCONTRADO, id)));
    }
}