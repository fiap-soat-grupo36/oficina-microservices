package br.com.fiap.oficina.shared.exception;

public class OrcamentoJaAprovadoException extends BusinessException {

    public OrcamentoJaAprovadoException(String message) {
        super(message);
    }

    public OrcamentoJaAprovadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
