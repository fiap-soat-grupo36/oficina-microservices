package br.com.fiap.oficina.notification;

import br.com.fiap.oficina.notification.exception.EmailNaoEnviadoException;
import br.com.fiap.oficina.notification.service.impl.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "sender", "oficina.fiap@gmail.com");
    }

    @Test
    void enviarEmail_deveEnviarComSucesso() {
        // Arrange
        String para = "cliente@example.com";
        String assunto = "Teste";
        String template = "teste-template";
        Map<String, Object> variaveis = Map.of("nome", "João");

        when(templateEngine.process(eq(template), any(Context.class))).thenReturn("<html>Teste</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.enviarEmail(para, assunto, template, variaveis);

        // Assert
        verify(templateEngine, times(1)).process(eq(template), any(Context.class));
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void enviarEmail_deveLancarExcecaoQuandoFalhar() throws MessagingException {
        // Arrange
        String para = "cliente@example.com";
        String assunto = "Teste";
        String template = "teste-template";
        Map<String, Object> variaveis = Map.of("nome", "João");

        when(templateEngine.process(eq(template), any(Context.class))).thenReturn("<html>Teste</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Erro ao enviar")).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            emailService.enviarEmail(para, assunto, template, variaveis);
        });

        verify(templateEngine, times(1)).process(eq(template), any(Context.class));
        verify(mailSender, times(1)).createMimeMessage();
    }
}
