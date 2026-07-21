package com.gestorReservas.Service;

import com.gestorReservas.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "email.provider", havingValue = "sendgrid", matchIfMissing = true)
public class SendGridEmailService implements EmailService {

    private final RestClient restClient;
    private final String from;

    public SendGridEmailService(
            @Value("${sendgrid.api-key}") String apiKey,
            @Value("${sendgrid.from}") String from) {
        this.from = from;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.sendgrid.com/v3")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        Map<String, Object> body = Map.of(
                "personalizations", List.of(
                        Map.of("to", List.of(Map.of("email", toEmail)))
                ),
                "from", parseFrom(from),
                "subject", "Tu código de verificación",
                "content", List.of(
                        Map.of(
                                "type", "text/html",
                                "value", "<p>Tu código es: <strong>" + code + "</strong></p>"
                                        + "<p>Caduca en 15 minutos.</p>"
                        )
                )
        );

        try {
            restClient.post()
                    .uri("/mail/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "no se pudo enviar el email de verificación");
        }
    }

    private Map<String, String> parseFrom(String from) {
        int start = from.indexOf('<');
        int end = from.indexOf('>');
        if (start >= 0 && end > start) {
            return Map.of(
                    "email", from.substring(start + 1, end).trim(),
                    "name", from.substring(0, start).trim()
            );
        }
        return Map.of("email", from.trim());
    }
}
