package com.gestorReservas.Controller;


import com.gestorReservas.Service.StripeService;
import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private final StripeService stripeService;

    public StripeWebhookController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload,
                                          @RequestHeader("Stripe-Signature") String sigHeader){

        stripeService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok("ok");
    }
}
