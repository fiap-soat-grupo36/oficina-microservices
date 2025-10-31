# Notification Service

Microserviço responsável pelo envio de notificações por email para os clientes da oficina.

## Descrição

O Notification Service é um microserviço dedicado ao envio de notificações por email, utilizando templates HTML e processamento assíncrono para garantir performance e escalabilidade.

## Funcionalidades

- Envio assíncrono de emails
- Templates HTML com Thymeleaf
- Notificação de orçamento disponível
- Notificação de veículo pronto para retirada
- Processamento de eventos do sistema

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.5.3
- Spring Mail
- Thymeleaf
- Lombok
- Maven

## Configuração

### Variáveis de Ambiente

```properties
EMAIL_USERNAME=seu-email@gmail.com
EMAIL_PASSWORD=sua-senha-app
```

### Configuração do Gmail

Para utilizar o Gmail como servidor SMTP:

1. Ative a verificação em duas etapas na sua conta Google
2. Gere uma senha de aplicativo específica
3. Use essa senha na variável `EMAIL_PASSWORD`

## Endpoints

O serviço não expõe endpoints REST, funciona através de Event Listeners.

## Eventos Suportados

### CalcularOrcamentoEvent
Disparado quando um novo orçamento precisa ser calculado.

### OrcamentoDisponivelEvent
Disparado quando um orçamento está disponível para o cliente, enviando um email com os detalhes.

### VeiculoDisponivelEvent
Disparado quando um veículo está pronto para retirada, enviando um email de notificação ao cliente.

## Templates de Email

### orcamento-disponivel.html
Template para notificação de orçamento disponível, contendo:
- Número do orçamento
- Lista de itens com descrição, quantidade e valores
- Valor total do orçamento

### veiculo-pronto.html
Template para notificação de veículo pronto, contendo:
- Informações do veículo (modelo e placa)
- Horários de funcionamento
- Instruções para retirada

## Executando o Serviço

```bash
# Na raiz do projeto
mvn clean install

# Executar o serviço
cd notification-service
mvn spring-boot:run
```

O serviço será iniciado na porta **8087**.

## Testes

```bash
mvn test
```

## Configuração do Pool de Threads

O serviço utiliza um pool de threads configurável para processamento assíncrono:

- Core Pool Size: 2
- Max Pool Size: 5
- Queue Capacity: 100

## Logs

O nível de log está configurado como DEBUG para facilitar o acompanhamento das operações:

```yaml
logging:
  level:
    br.com.fiap.oficina.notification: DEBUG
```

## Tratamento de Erros

O serviço possui tratamento específico para falhas no envio de emails através do `ApiExceptionHandler`, retornando mensagens amigáveis em caso de erro.

## Arquitetura

O serviço segue uma arquitetura em camadas:

- **Listener**: Escuta eventos do sistema
- **Service**: Lógica de negócio
- **Model**: Modelos de dados simplificados
- **Exception**: Tratamento de exceções
- **Config**: Configurações do sistema

## Autor

FIAP - Grupo 36
