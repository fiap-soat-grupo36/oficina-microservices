package br.com.fiap.oficina.notification.exception;

public class EmailNaoEnviadoException extends RuntimeException {
    public EmailNaoEnviadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
