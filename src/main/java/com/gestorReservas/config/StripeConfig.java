package com.gestorReservas.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.price-id}")
    private String priceId;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
    public String getWebhookSecret() {
        return webhookSecret;
    }
    public String getPriceId() {
        return priceId;
    }
    public String getSuccessUrl() {
        return successUrl;
    }
    public String getCancelUrl() {
        return cancelUrl;
    }
}
