package com.municipioactivo.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String mailHost;
    private final String fromAddress;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${MAIL_HOST:}") String mailHost,
            @Value("${MAIL_FROM:}") String fromAddress) {
        this.mailSenderProvider = mailSenderProvider;
        this.mailHost = mailHost;
        this.fromAddress = fromAddress;
    }

    public boolean enviarConfirmacionReclamo(String email, String codigoSeguimiento) {
        if (mailHost.isBlank()) {
            logger.warn("No se envió la confirmación del reclamo: MAIL_HOST no está configurado.");
            return false;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            logger.warn("No se envió la confirmación del reclamo: no hay un remitente de correo configurado.");
            return false;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (!fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(email);
        message.setSubject("Confirmación de reclamo - Municipio Activo");
        message.setText("""
                Registramos tu reclamo en Municipio Activo.

                Código de seguimiento: %s

                Conservá este código para consultar el estado de tu solicitud en el portal.
                """.formatted(codigoSeguimiento));

        try {
            mailSender.send(message);
            return true;
        } catch (MailException exception) {
            logger.warn("No se pudo enviar la confirmación del reclamo.", exception);
            return false;
        }
    }
}
