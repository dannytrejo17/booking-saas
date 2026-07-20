package com.gestorReservas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void me_sinToken_bloqueaAcceso() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void billingCheckout_sinToken_bloqueaAcceso() throws Exception {
        mockMvc.perform(post("/api/billing/checkout"))
                .andExpect(status().isForbidden());
    }

    @Test
    void stripeWebhook_sinToken_permiteAcceso() throws Exception {
        mockMvc.perform(post("/api/stripe/webhook")
                        .contentType("application/json")
                        .content("{}")
                        .header("Stripe-Signature", "t=1,v1=invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicEndpoint_sinToken_permiteAcceso() throws Exception {
        mockMvc.perform(get("/api/public/negocio-inexistente"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("ruta pública no debería exigir auth, status=" + status);
                    }
                });
    }
}
