package com.municipioactivo.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarMailConfirmacion(Reclamo reclamo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(reclamo.getContribuyenteEmail());
        message.setSubject("Confirmación de Reclamo - Código: " + reclamo.getCodigoSeguimiento());

        String contenido = String.format(
            "Estimado/a contribuyente,\n\n" +
            "Su reclamo ha sido registrado exitosamente.\n\n" +
            "Resumen del reclamo:\n" +
            "- Número de seguimiento: %s\n" +
            "- Categoría: %s\n" +
            "- Dirección: %s\n" +
            "- Descripción: %s\n\n" +
            "Conserve su número de seguimiento para posteriores consultas.\n\n" +
            "Atentamente,\nAtención al Ciudadano",
            reclamo.getCodigoSeguimiento(),
            reclamo.getCategoria(),
            reclamo.getDireccion(),
            reclamo.getDescripcion()
        );

        message.setText(contenido);
        mailSender.send(message);
    }
}