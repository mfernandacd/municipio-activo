package com.municipioactivo.backend;

import com.municipioactivo.backend.model.Reclamo;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarMailConfirmacion(Reclamo reclamo) {
        if (reclamo.getEmailUsuario() == null || reclamo.getEmailUsuario().isBlank()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(reclamo.getEmailUsuario());
        message.setSubject("Confirmación de Reclamo - Municipio Activo (" + reclamo.getCodigoSeguimiento() + ")");
        
        String contenido = String.format(
            "Estimado/a contribuyente,\n\n" +
            "Se ha registrado exitosamente su reclamo en la plataforma Municipio Activo.\n\n" +
            "📋 RESUMEN DE LA SOLICITUD:\n" +
            "-------------------------------------------\n" +
            "📌 Código de Seguimiento: %s\n" +
            "📝 Detalle: %s\n" +
            "-------------------------------------------\n\n" +
            "Guarde este código para consultar el estado de su trámite en el portal web.\n\n" +
            "Atentamente,\n" +
            "Gestión Municipal - Municipio Activo",
            reclamo.getCodigoSeguimiento(),
            reclamo.getDescripcion()
        );

        message.setText(contenido);
        mailSender.send(message);
    }
}