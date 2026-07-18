package com.gestorReservas.Service;

import com.gestorReservas.exception.ApiException;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;
    private final String from;

    public EmailService(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from}") String from) {
        this.resend = new Resend(apiKey);
        this.from = from;
    }

    public void sendVerificationCode(String toEmail, String code) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(from)
                .to(toEmail)
                .subject("Tu código de verificación")
                .html("<p>Tu código es: <strong>" + code + "</strong></p>"
                        + "<p>Caduca en 15 minutos.</p>")
                .build();
        try {
            CreateEmailResponse data = resend.emails().send(params);
            // opcional: loguear data.getId()
        } catch (ResendException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "no se pudo enviar el email de verificación");
        }
    }
}

