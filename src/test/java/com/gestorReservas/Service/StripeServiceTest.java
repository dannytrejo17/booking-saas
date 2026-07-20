package com.gestorReservas.Service;

import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.BusinessRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.config.StripeConfig;
import com.gestorReservas.exception.ApiException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeServiceTest {

    @Mock
    private StripeConfig stripeConfig;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private StripeService stripeService;

    private Principal principal(String email) {
        return () -> email;
    }

    @Test
    void createCheckoutSession_usuarioNoExiste_lanzaUnauthorized() {
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> stripeService.createCheckoutSession(principal("owner@test.com")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("no autenticado", ex.getMessage());
    }

    @Test
    void createCheckoutSession_sinNegocio_lanzaBadRequest() {
        User user = new User();
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));

        ApiException ex = assertThrows(ApiException.class,
                () -> stripeService.createCheckoutSession(principal("owner@test.com")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("no tienes negocio", ex.getMessage());
    }

    @Test
    void createCheckoutSession_conNegocio_devuelveUrl() {
        Business business = new Business();
        business.setBusinessId(10L);
        business.setEmail("negocio@test.com");
        User user = new User();
        user.setBusiness(business);
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(stripeConfig.getSuccessUrl()).thenReturn("http://localhost/success");
        when(stripeConfig.getCancelUrl()).thenReturn("http://localhost/cancel");
        when(stripeConfig.getPriceId()).thenReturn("price_test");

        Session session = mock(Session.class);
        when(session.getUrl()).thenReturn("https://checkout.stripe.com/test");

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            sessionMock.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(session);

            String url = stripeService.createCheckoutSession(principal("owner@test.com"));

            assertEquals("https://checkout.stripe.com/test", url);
        }
    }

    @Test
    void handleWebhook_firmaInvalida_lanzaBadRequest() {
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test");

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenThrow(new SignatureVerificationException("invalid", "sig"));

            ApiException ex = assertThrows(ApiException.class,
                    () -> stripeService.handleWebhook("{}", "t=1,v1=bad"));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
            assertEquals("firma de webhook inválida", ex.getMessage());
        }
    }

    @Test
    void handleWebhook_checkoutCompleted_activaSuscripcion() {
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test");

        Business business = new Business();
        business.setBusinessId(10L);
        business.setSubscriptionStatus("inactive");
        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenAnswer(inv -> inv.getArgument(0));

        Event event = mock(Event.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        Session session = mock(Session.class);
        when(event.getType()).thenReturn("checkout.session.completed");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(session));
        when(session.getMetadata()).thenReturn(Map.of("businessId", "10"));
        when(session.getCustomer()).thenReturn("cus_123");
        when(session.getSubscription()).thenReturn("sub_123");

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(eq("{}"), eq("sig"), eq("whsec_test")))
                    .thenReturn(event);

            stripeService.handleWebhook("{}", "sig");
        }

        ArgumentCaptor<Business> captor = ArgumentCaptor.forClass(Business.class);
        verify(businessRepository).save(captor.capture());
        assertEquals("active", captor.getValue().getSubscriptionStatus());
        assertEquals("cus_123", captor.getValue().getStripeCustomerId());
        assertEquals("sub_123", captor.getValue().getStripeSubscriptionId());
    }

    @Test
    void handleWebhook_subscriptionDeleted_marcaCanceled() {
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test");

        Business business = new Business();
        business.setBusinessId(10L);
        business.setSubscriptionStatus("active");
        when(businessRepository.findByStripeSubscriptionId("sub_123")).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenAnswer(inv -> inv.getArgument(0));

        Event event = mock(Event.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        Subscription subscription = mock(Subscription.class);
        when(event.getType()).thenReturn("customer.subscription.deleted");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(subscription));
        when(subscription.getId()).thenReturn("sub_123");

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenReturn(event);

            stripeService.handleWebhook("{}", "sig");
        }

        ArgumentCaptor<Business> captor = ArgumentCaptor.forClass(Business.class);
        verify(businessRepository).save(captor.capture());
        assertEquals("canceled", captor.getValue().getSubscriptionStatus());
    }

    @Test
    void handleWebhook_invoicePaymentFailed_marcaPastDue() {
        when(stripeConfig.getWebhookSecret()).thenReturn("whsec_test");

        Business business = new Business();
        business.setBusinessId(10L);
        business.setSubscriptionStatus("active");
        when(businessRepository.findByStripeCustomerId("cus_123")).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenAnswer(inv -> inv.getArgument(0));

        Event event = mock(Event.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        Invoice invoice = mock(Invoice.class);
        when(event.getType()).thenReturn("invoice.payment_failed");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(invoice));
        when(invoice.getCustomer()).thenReturn("cus_123");

        try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class)) {
            webhookMock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenReturn(event);

            stripeService.handleWebhook("{}", "sig");
        }

        ArgumentCaptor<Business> captor = ArgumentCaptor.forClass(Business.class);
        verify(businessRepository).save(captor.capture());
        assertEquals("past_due", captor.getValue().getSubscriptionStatus());
    }
}
