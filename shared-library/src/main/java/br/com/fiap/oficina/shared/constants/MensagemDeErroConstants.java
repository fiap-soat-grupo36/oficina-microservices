package br.com.fiap.oficina.shared.constants;

public class MensagemDeErroConstants {

    private MensagemDeErroConstants() {
    }

    // Cliente
    public static final String CLIENTE_NAO_ENCONTRADO = "Cliente não encontrado com ID: %s";

    // Veículo
    public static final String VEICULO_NAO_ENCONTRADO = "Veículo não encontrado com ID: %s";
    public static final String VEICULO_PLACA_NAO_ENCONTRADO = "Veículo não encontrado com placa: %s";

    // Serviço
    public static final String SERVICO_NAO_ENCONTRADO = "Serviço não encontrado com ID: %s";

    // Produto
    public static final String PRODUTO_NAO_ENCONTRADO = "Produto não encontrado com ID: %s";

    // Ordem de Serviço
    public static final String OS_NAO_ENCONTRADA = "Ordem de serviço não encontrada com ID: %s";
    public static final String OS_AGUARDANDO_APROVACAO = "Ordem de serviço está aguardando aprovação do orçamento";

    // Orçamento
    public static final String ORCAMENTO_NAO_ENCONTRADO = "Orçamento não encontrado com ID: %s";

    // Usuário
    public static final String USUARIO_NAO_ENCONTRADO = "Usuário não encontrado com ID: %s";
}