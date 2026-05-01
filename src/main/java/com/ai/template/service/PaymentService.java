package com.ai.template.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.ai.template.model.entity.PaymentRecord;

import java.util.List;

public interface PaymentService {

    String createVipPaymentSession(Long userId) throws StripeException;

    void handlePaymentSuccess(Session session);

    boolean handleRefund(Long userId, String reason) throws StripeException;

    Event constructEvent(String payload, String sigHeader) throws Exception;

    List<PaymentRecord> getPaymentRecords(Long userId);
}
