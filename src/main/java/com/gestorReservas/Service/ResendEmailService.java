package com.gestorReservas.Service;

import com.gestorReservas.exception.ApiException;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "email.provider", havingValue = "resend")
public class ResendEmailService implements EmailService {

    private final Resend resend;
    private final String from;

    public ResendEmailService(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from}") String from) {
        this.resend = new Resend(apiKey);
        this.from = from;
    }

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(from)
                .to(toEmail)
                .subject("Tu código de verificación")
                .html("<p>Tu código es: <strong>" + code + "</strong></p>"
                        + "<p>Caduca en 15 minutos.</p>")
                .build();
        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "no se pudo enviar el email de verificación");
        }
    }
}
