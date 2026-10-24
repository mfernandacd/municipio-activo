package com.municipioactivo.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

class EmailServiceTests {

    @Test
    void sendsOnlyTheTrackingCodeToTheAccountEmail() {
        ObjectProvider<JavaMailSender> senderProvider = mock(ObjectProvider.class);
        JavaMailSender sender = mock(JavaMailSender.class);
        when(senderProvider.getIfAvailable()).thenReturn(sender);
        EmailService emailService = new EmailService(senderProvider, "smtp.example.com", "noreply@example.com");

        boolean sent = emailService.enviarConfirmacionReclamo("usuario@example.com", "ABC123DEF456");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertTrue(sent);
        assertEquals("usuario@example.com", message.getTo()[0]);
        assertEquals("noreply@example.com", message.getFrom());
        assertTrue(message.getText().contains("ABC123DEF456"));
        assertFalse(message.getText().contains("descripción"));
    }

    @Test
    void reportsWhenMailHostIsNotConfigured() {
        ObjectProvider<JavaMailSender> senderProvider = mock(ObjectProvider.class);
        EmailService emailService = new EmailService(senderProvider, "", "");

        assertFalse(emailService.enviarConfirmacionReclamo("usuario@example.com", "ABC123DEF456"));
    }

    @Test
    void reportsMailProviderFailuresWithoutFailingTheClaimFlow() {
        ObjectProvider<JavaMailSender> senderProvider = mock(ObjectProvider.class);
        JavaMailSender sender = mock(JavaMailSender.class);
        when(senderProvider.getIfAvailable()).thenReturn(sender);
        org.mockito.Mockito.doThrow(new MailSendException("SMTP unavailable"))
                .when(sender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
        EmailService emailService = new EmailService(senderProvider, "smtp.example.com", "");

        assertFalse(emailService.enviarConfirmacionReclamo("usuario@example.com", "ABC123DEF456"));
    }
}
