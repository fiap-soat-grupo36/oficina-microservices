package br.com.fiap.oficina.shared.exception;

public class OrcamentoComDependenciasException extends BusinessException {

    public OrcamentoComDependenciasException(String message) {
        super(message);
    }

    public OrcamentoComDependenciasException(String message, Throwable cause) {
        super(message, cause);
    }
}
