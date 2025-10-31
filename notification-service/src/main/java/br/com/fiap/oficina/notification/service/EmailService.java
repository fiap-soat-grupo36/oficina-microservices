package br.com.fiap.oficina.notification.service;

import java.util.Map;

public interface EmailService {
    void enviarEmail(String para, String assunto, String template, Map<String, Object> variaveis);
}
