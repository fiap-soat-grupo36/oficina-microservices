package br.com.fiap.oficina.shared.exception;

public class QuantidadeInvalidaParaReservaException extends BusinessException {

    public QuantidadeInvalidaParaReservaException(String message) {
        super(message);
    }

    public QuantidadeInvalidaParaReservaException(String message, Throwable cause) {
        super(message, cause);
    }
}
