package br.com.fiap.oficina.shared.exception;

public class EstoqueInsuficienteException extends BusinessException {

    public EstoqueInsuficienteException(String message) {
        super(message);
    }

    public EstoqueInsuficienteException(String message, Throwable cause) {
        super(message, cause);
    }
}
