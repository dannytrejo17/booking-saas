package com.gestorReservas.Service;

import com.gestorReservas.Model.Business;
import com.gestorReservas.Model.User;
import com.gestorReservas.Repository.BusinessRepository;
import com.gestorReservas.Repository.UserRepository;
import com.gestorReservas.config.StripeConfig;
import com.gestorReservas.exception.ApiException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
public class StripeService {

    private final StripeConfig stripeConfig;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    public StripeService(StripeConfig stripeConfig,
                         UserRepository userRepository,
                         BusinessRepository businessRepository) {
        this.stripeConfig = stripeConfig;
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
    }

    public String createCheckoutSession(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "no autenticado"));
        Business business = user.getBusiness();
        if (business == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "no tienes negocio");
        }

        try {
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(stripeConfig.getSuccessUrl())
                    .setCancelUrl(stripeConfig.getCancelUrl())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(stripeConfig.getPriceId())
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putMetadata("businessId", business.getBusinessId().toString())
                    .setClientReferenceId(business.getBusinessId().toString());

            if (business.getStripeCustomerId() != null && !business.getStripeCustomerId().isBlank()) {
                builder.setCustomer(business.getStripeCustomerId());
            } else if (business.getEmail() != null && !business.getEmail().isBlank()) {
                builder.setCustomerEmail(business.getEmail());
            }

            Session session = Session.create(builder.build());
            return session.getUrl();
        } catch (StripeException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "error creando checkout: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "firma de webhook inválida");
        }

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = deserializer.getObject()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "no se pudo deserializar el evento"));

        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted((Session) stripeObject);
            case "customer.subscription.updated" -> handleSubscriptionUpdated((Subscription) stripeObject);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted((Subscription) stripeObject);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice) stripeObject);
            default -> {
            }
        }
    }

    private void handleCheckoutCompleted(Session session) {
        String businessIdValue = null;
        if (session.getMetadata() != null) {
            businessIdValue = session.getMetadata().get("businessId");
        }
        if (businessIdValue == null || businessIdValue.isBlank()) {
            businessIdValue = session.getClientReferenceId();
        }
        if (businessIdValue == null || businessIdValue.isBlank()) {
            return;
        }

        Business business = businessRepository.findById(Long.valueOf(businessIdValue))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "negocio no encontrado"));

        if (session.getCustomer() != null) {
            business.setStripeCustomerId(session.getCustomer());
        }
        if (session.getSubscription() != null) {
            business.setStripeSubscriptionId(session.getSubscription());
        }
        business.setSubscriptionStatus("active");
        businessRepository.save(business);
    }

    private void handleSubscriptionUpdated(Subscription subscription) {
        Business business = businessRepository.findByStripeSubscriptionId(subscription.getId()).orElse(null);
        if (business == null) {
            return;
        }
        business.setSubscriptionStatus(mapStatus(subscription.getStatus()));
        businessRepository.save(business);
    }

    private void handleSubscriptionDeleted(Subscription subscription) {
        Business business = businessRepository.findByStripeSubscriptionId(subscription.getId()).orElse(null);
        if (business == null) {
            return;
        }
        business.setSubscriptionStatus("canceled");
        businessRepository.save(business);
    }

    private void handleInvoicePaymentFailed(Invoice invoice) {
        if (invoice.getCustomer() == null) {
            return;
        }
        Business business = businessRepository.findByStripeCustomerId(invoice.getCustomer()).orElse(null);
        if (business == null) {
            return;
        }
        business.setSubscriptionStatus("past_due");
        businessRepository.save(business);
    }

    private String mapStatus(String stripeStatus) {
        if (stripeStatus == null) {
            return "inactive";
        }
        return switch (stripeStatus) {
            case "active", "trialing" -> "active";
            case "past_due", "unpaid" -> "past_due";
            case "canceled", "incomplete_expired" -> "canceled";
            default -> stripeStatus;
        };
    }
}
