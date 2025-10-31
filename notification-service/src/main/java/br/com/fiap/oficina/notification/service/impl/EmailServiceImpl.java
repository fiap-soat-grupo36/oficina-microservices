package br.com.fiap.oficina.notification.service.impl;

import br.com.fiap.oficina.notification.exception.EmailNaoEnviadoException;
import br.com.fiap.oficina.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.notification.email.sender}")
    private String sender;

    @Async("taskExecutor")
    public void enviarEmail(String para, String assunto, String template, Map<String, Object> variaveis) {
        log.info("Iniciando envio de email para: {}", para);
        try {
            Context context = new Context();
            context.setVariables(variaveis);

            String bodyHtml = templateEngine.process(template, context);

            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, "UTF-8");

            helper.setFrom(sender);
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(bodyHtml, true);

            mailSender.send(mensagem);
            log.info("Email enviado com sucesso para: {}", para);
        } catch (MessagingException e) {
            throw new EmailNaoEnviadoException("Erro ao enviar o e-mail", e);
        }
    }
}
