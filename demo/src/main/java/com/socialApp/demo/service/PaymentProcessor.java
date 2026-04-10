package com.socialApp.demo.service;

import com.socialApp.demo.dto.request.CheckoutRequest;
import com.socialApp.demo.entity.Plan;
import com.socialApp.demo.entity.Subscription;
import com.socialApp.demo.entity.Users;
import com.socialApp.demo.enums.SubscriptionStatus;
import com.socialApp.demo.exception.ResourceNotFoundException;
import com.socialApp.demo.repository.PlanRepository;
import com.socialApp.demo.repository.SubscriptionRepository;
import com.socialApp.demo.repository.UserRepository;
import com.socialApp.demo.security.AuthUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessor {
    @Value("${client.url}")
    private String frontendUrl;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuthUtil authUtil;

    public ResponseEntity<Map<String, String>> createCheckoutSessionUrl(CheckoutRequest checkoutRequest) throws StripeException {
        Plan plan = planRepository.findById(checkoutRequest.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", checkoutRequest.planId().toString()));

        Long userId = authUtil.getCurrentLoggedInUserId();
        Users user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("user", userId.toString()));

        var params = SessionCreateParams.builder()
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(plan.getPriceId())
                        .setQuantity(1L)
                        .build()
                )
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(
                        new SessionCreateParams.SubscriptionData.Builder()
                                .setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder()
                                        .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                        .build())
                                .build()
                )
                .setSuccessUrl(frontendUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/payment/cancel")
                .putMetadata("user_id", userId.toString())
                .putMetadata("plan_id", plan.getId().toString());

        Session session = Session.create(params.build());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", session.getUrl()));
    }

    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        switch (type) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted((Session) stripeObject, metadata); // one-time, on checkout completed
            case "customer.subscription.updated" -> handleCustomerSubscriptionUpdated((com.stripe.model.Subscription) stripeObject); // when user cancels, upgrades or any updates
            case "customer.subscription.deleted" -> handleCustomerSubscriptionDeleted((com.stripe.model.Subscription) stripeObject); // when subscription ends, revoke the access
            case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice) stripeObject); // when invoice is not paid, mark as PAST_DUE
            default -> log.debug("Ignoring the event: {}", type);
        }
    }

    public void handleCheckoutSessionCompleted(Session session, Map<String, String> metadata){
        if(session == null) {
            log.error("session object was null");
            return;
        }
        String subscriptionId = session.getSubscription();

        System.out.println("Hittttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"+subscriptionId);

        Plan plan = planRepository.findById(Long.parseLong(metadata.get("plan_id")))
                .orElseThrow(() -> new ResourceNotFoundException("Plan", metadata.get("plan_id")));

        System.out.println("Hittttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"+plan.getId());

        Users user = userRepository.findById(Long.parseLong(metadata.get("user_id")))
                .orElseThrow(() -> new ResourceNotFoundException("User", metadata.get("user_id")));

        System.out.println("Hittttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"+user.getId());

        Subscription subscription = new Subscription();
        subscription.setPlan(plan);
        subscription.setUser(user);
        subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        subscription.setStripeSubscriptionId(subscriptionId);
        subscription.setExpiresAt(Instant.now().plus(31, ChronoUnit.DAYS));

        subscriptionRepository.save(subscription);
        System.out.println("Hittttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"+"subs created");

    }

    public void handleCustomerSubscriptionUpdated(com.stripe.model.Subscription subscription){
        if (subscription == null) {
            return;
        }

        SubscriptionStatus subscriptionStatus = mapStripeStatusToEnum(subscription.getStatus());
        if(subscriptionStatus == null){
            return;
        }

        SubscriptionItem item = subscription.getItems().getData().get(0);
        Instant newStartDate = toInstant(item.getCurrentPeriodStart());
        Instant newEndDate = toInstant(item.getCurrentPeriodEnd());

        Subscription updatedSubscription = getSubscription(subscription.getId());
        updatedSubscription.setStartedAt(newStartDate);
        updatedSubscription.setExpiresAt(newEndDate);
        subscriptionRepository.save(updatedSubscription);
    }

    private void handleCustomerSubscriptionDeleted(com.stripe.model.Subscription subscription) {
        if (subscription == null) {
            log.error("subscription object was null inside handleCustomerSubscriptionDeleted");
            return;
        }
        Subscription toBeCanceledSubscription = getSubscription(subscription.getId());
        toBeCanceledSubscription.setSubscriptionStatus(SubscriptionStatus.CANCELED);
        subscriptionRepository.save(toBeCanceledSubscription);
    }

    private void handleInvoicePaymentFailed(Invoice invoice) {
        String subId = extractSubscriptionId(invoice);
        if(subId == null) return;

        Subscription subscription = getSubscription(subId);
        subscription.setSubscriptionStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);
    }

    private SubscriptionStatus mapStripeStatusToEnum(String status) {
        return switch (status) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "past_due", "unpaid", "paused", "incomplete_expired" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            default -> {
                log.warn("Unmapped Stripe status: {}", status);
                yield null;
            }
        };
    }

    public Subscription getSubscription(String id){
        return subscriptionRepository.findByStripeSubscriptionId(id);
    }

    private String extractSubscriptionId(Invoice invoice) {
        var parent = invoice.getParent();
        if (parent == null) return null;

        var subDetails = parent.getSubscriptionDetails();
        if (subDetails == null) return null;

        return subDetails.getSubscription();
    }

    private Instant toInstant(Long epoch) {
        return epoch != null ? Instant.ofEpochSecond(epoch) : null;
    }
}
