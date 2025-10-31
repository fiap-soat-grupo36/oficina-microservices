package br.com.fiap.oficina.notification;

import br.com.fiap.oficina.notification.event.CalcularOrcamentoEvent;
import br.com.fiap.oficina.notification.event.OrcamentoDisponivelEvent;
import br.com.fiap.oficina.notification.event.VeiculoDisponivelEvent;
import br.com.fiap.oficina.notification.listener.NotificacaoEventListener;
import br.com.fiap.oficina.notification.model.*;
import br.com.fiap.oficina.notification.service.EmailService;
import br.com.fiap.oficina.notification.service.OrcamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoEventListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private OrcamentoService orcamentoService;

    @InjectMocks
    private NotificacaoEventListener listener;

    private Cliente cliente;
    private Veiculo veiculo;
    private OrdemServico ordemServico;
    private Orcamento orcamento;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@example.com");
        cliente.setTelefone("11999999999");

        veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setPlaca("ABC-1234");
        veiculo.setModelo("Civic");
        veiculo.setMarca("Honda");
        veiculo.setAno(2020);

        ordemServico = new OrdemServico();
        ordemServico.setId(1L);
        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        ordemServico.setDataCriacao(LocalDateTime.now());

        ItemOrcamento item = new ItemOrcamento();
        item.setId(1L);
        item.setDescricao("Troca de óleo");
        item.setQuantidade(1);
        item.setValorUnitario(BigDecimal.valueOf(100.00));
        item.setValorTotal(BigDecimal.valueOf(100.00));

        List<ItemOrcamento> itens = new ArrayList<>();
        itens.add(item);

        orcamento = new Orcamento();
        orcamento.setId(1L);
        orcamento.setItensOrcamento(itens);
        orcamento.setValorTotal(BigDecimal.valueOf(100.00));
        orcamento.setDataCriacao(LocalDateTime.now());
    }

    @Test
    void handleCalcularOrcamento_deveCalcularOrcamento() {
        // Arrange
        CalcularOrcamentoEvent event = new CalcularOrcamentoEvent(ordemServico);

        // Act
        listener.handleCalcularOrcamento(event);

        // Assert
        verify(orcamentoService, times(1)).criar(ordemServico);
    }

    @Test
    void handleOrcamentoDisponivel_deveEnviarEmail() {
        // Arrange
        OrcamentoDisponivelEvent event = new OrcamentoDisponivelEvent(ordemServico, orcamento);

        // Act
        listener.handleOrcamentoDisponivel(event);

        // Assert
        verify(emailService, times(1)).enviarEmail(
                eq("joao@example.com"),
                eq("Seu orçamento já está disponível"),
                eq("orcamento-disponivel"),
                anyMap()
        );
    }

    @Test
    void handleVeiculoPronto_deveEnviarEmail() {
        // Arrange
        VeiculoDisponivelEvent event = new VeiculoDisponivelEvent(cliente, veiculo, "token123");

        // Act
        listener.handleVeiculoPronto(event);

        // Assert
        verify(emailService, times(1)).enviarEmail(
                eq("joao@example.com"),
                eq("Seu veículo está pronto para retirada"),
                eq("veiculo-pronto"),
                anyMap()
        );
    }
}
