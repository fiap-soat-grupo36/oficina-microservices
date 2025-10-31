package br.com.fiap.oficina.shared.exception;

public class RoleInvalidaException extends RuntimeException {

    public RoleInvalidaException(String message) {
        super(message);
    }

    public RoleInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
