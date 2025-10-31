package br.com.fiap.oficina.notification.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(EmailNaoEnviadoException.class)
    public ResponseEntity<String> handleEmailNaoEnviado(EmailNaoEnviadoException ex) {
        log.error("Erro ao enviar email: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Erro ao enviar notificação por email");
    }
}
