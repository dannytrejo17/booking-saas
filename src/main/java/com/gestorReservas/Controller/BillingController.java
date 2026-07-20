package com.gestorReservas.Controller;

import com.gestorReservas.Service.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final StripeService stripeService;

    public BillingController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String,String>> checkout(Principal principal){
        String url = stripeService.createCheckoutSession(principal);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("url", url));
    }
}
