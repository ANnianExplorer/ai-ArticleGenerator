package com.ai.template.controller;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.ai.template.service.PaymentService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/webhook")
@Slf4j
@Hidden
public class StripeWebhookController {

    @Resource
    private PaymentService paymentService;

    
    @PostMapping("/stripe")
    public String handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        try {
            Event event = paymentService.constructEvent(payload, sigHeader);
            
            log.info("Received Stripe Webhook event, type={}", event.getType());
            
            switch (event.getType()) {
                case "checkout.session.completed":
                    Session session = (Session) event.getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow(() -> new RuntimeException("Cannot parse Session object"));
                    paymentService.handlePaymentSuccess(session);
                    break;
                    
                case "checkout.session.async_payment_succeeded":
                    Session asyncSession = (Session) event.getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow(() -> new RuntimeException("Cannot parse Session object"));
                    paymentService.handlePaymentSuccess(asyncSession);
                    break;
                    
                default:
                    log.info("Unhandled event type: {}", event.getType());
                    break;
            }
            
            return "success";
        } catch (Exception e) {
            log.error("Handle Stripe Webhook failed", e);
            return "error";
        }
    }
}