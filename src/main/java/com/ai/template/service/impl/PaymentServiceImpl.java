package com.ai.template.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.ai.template.config.StripeConfig;
import com.ai.template.constant.UserConstant;
import com.ai.template.exception.BusinessException;
import com.ai.template.exception.ErrorCode;
import com.ai.template.mapper.PaymentRecordMapper;
import com.ai.template.mapper.UserMapper;
import com.ai.template.model.entity.PaymentRecord;
import com.ai.template.model.entity.User;
import com.ai.template.model.enums.PaymentStatusEnum;
import com.ai.template.model.enums.ProductTypeEnum;
import com.ai.template.service.PaymentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final String CURRENCY_USD = "usd";
    private static final long CENTS_MULTIPLIER = 100L;

    @Resource
    private StripeConfig stripeConfig;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PaymentRecordMapper paymentRecordMapper;

    @Override
    public String createVipPaymentSession(Long userId) throws StripeException {
        User user = getUserOrThrow(userId);
        validateNotVip(user);

        ProductTypeEnum productType = ProductTypeEnum.VIP_PERMANENT;
        Session session = createStripeSession(userId, productType);
        savePaymentRecord(userId, session, productType);

        log.info("创建支付会话成功, userId={}, sessionId={}", userId, session.getId());
        return session.getUrl();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentSuccess(Session session) {
        String sessionId = session.getId();
        String userId = session.getMetadata().get("userId");
        String paymentIntentId = session.getPaymentIntent();

        PaymentRecord record = findPaymentRecordBySessionId(sessionId);
        if (record == null) {
            log.warn("支付记录不存�? sessionId={}", sessionId);
            return;
        }

            log.info("支付记录已处�? sessionId={}", sessionId);
            return;
        }

        updatePaymentStatus(record.getId(), PaymentStatusEnum.SUCCEEDED, paymentIntentId);
        upgradeUserToVip(Long.valueOf(userId));

        log.info("支付成功，用户已升级�?VIP, userId={}, sessionId={}", userId, sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRefund(Long userId, String reason) throws StripeException {
        User user = getUserOrThrow(userId);
        validateIsVip(user);

        PaymentRecord paymentRecord = findLatestSuccessfulPayment(userId);
        if (paymentRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到支付记�?);
        }

        if (paymentRecord.getStripePaymentIntentId() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "支付记录无效");
        }

        Refund refund = createStripeRefund(paymentRecord.getStripePaymentIntentId());
        if (!"succeeded".equals(refund.getStatus())) {
            return false;
        }

        updateRefundRecord(paymentRecord.getId(), reason);
        revokeVipStatus(userId);

        log.info("退款成功，已取�?VIP 身份, userId={}, refundId={}", userId, refund.getId());
        return true;
    }

    @Override
    public Event constructEvent(String payload, String sigHeader) throws Exception {
        return Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
    }

    @Override
    public List<PaymentRecord> getPaymentRecords(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .orderBy("createTime", false);
        return paymentRecordMapper.selectListByQuery(queryWrapper);
    }

    private User getUserOrThrow(Long userId) {
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存�?);
        }
        return user;
    }

    private void validateNotVip(User user) {
        if (UserConstant.VIP_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已经是永久会员");
        }
    }

    private void validateIsVip(User user) {
        if (!UserConstant.VIP_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您不是会员，无法退�?);
        }
    }

    private Session createStripeSession(Long userId, ProductTypeEnum productType) throws StripeException {
        long amountInCents = productType.getPrice().multiply(new BigDecimal(CENTS_MULTIPLIER)).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeConfig.getSuccessUrl())
                .setCancelUrl(stripeConfig.getCancelUrl())
                .addLineItem(buildLineItem(productType, amountInCents))
                .putMetadata("userId", String.valueOf(userId))
                .putMetadata("productType", productType.getValue())
                .build();

        return Session.create(params);
    }

    
    private SessionCreateParams.LineItem buildLineItem(ProductTypeEnum productType, long amountInCents) {
        return SessionCreateParams.LineItem.builder()
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(CURRENCY_USD)
                                .setUnitAmount(amountInCents)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(productType.getDescription())
                                                .setDescription("解锁全部高级功能，无限创作配额，终身有效")
                                                .build()
                                )
                                .build()
                )
                .setQuantity(1L)
                .build();
    }

    private void savePaymentRecord(Long userId, Session session, ProductTypeEnum productType) {
        PaymentRecord record = PaymentRecord.builder()
                .userId(userId)
                .stripeSessionId(session.getId())
                .amount(productType.getPrice())
                .currency(CURRENCY_USD)
                .status(PaymentStatusEnum.PENDING.getValue())
                .productType(productType.getValue())
                .description(productType.getDescription())
                .build();
        paymentRecordMapper.insert(record);
    }

    private PaymentRecord findPaymentRecordBySessionId(String sessionId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("stripeSessionId", sessionId);
        return paymentRecordMapper.selectOneByQuery(queryWrapper);
    }

    private PaymentRecord findLatestSuccessfulPayment(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId)
                .eq("status", PaymentStatusEnum.SUCCEEDED.getValue())
                .eq("productType", ProductTypeEnum.VIP_PERMANENT.getValue())
                .orderBy("createTime", false)
                .limit(1);
        return paymentRecordMapper.selectOneByQuery(queryWrapper);
    }

    private void updatePaymentStatus(Long recordId, PaymentStatusEnum status, String paymentIntentId) {
        PaymentRecord updateRecord = new PaymentRecord();
        updateRecord.setId(recordId);
        updateRecord.setStatus(status.getValue());
        updateRecord.setStripePaymentIntentId(paymentIntentId);
        paymentRecordMapper.update(updateRecord);
    }

    private void upgradeUserToVip(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setVipTime(LocalDateTime.now());
        user.setUserRole(UserConstant.VIP_ROLE);
        userMapper.update(user);
    }

    private Refund createStripeRefund(String paymentIntentId) throws StripeException {
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();
        return Refund.create(params);
    }

    private void updateRefundRecord(Long recordId, String reason) {
        PaymentRecord updateRecord = new PaymentRecord();
        updateRecord.setId(recordId);
        updateRecord.setStatus(PaymentStatusEnum.REFUNDED.getValue());
        updateRecord.setRefundTime(LocalDateTime.now());
        updateRecord.setRefundReason(reason);
        paymentRecordMapper.update(updateRecord);
    }

    private void revokeVipStatus(Long userId) {
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setVipTime(null);
        updateUser.setUserRole(UserConstant.DEFAULT_ROLE);
        updateUser.setQuota(UserConstant.DEFAULT_QUOTA);
        userMapper.update(updateUser);
    }
}
