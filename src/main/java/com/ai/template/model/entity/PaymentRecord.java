package com.ai.template.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "payment_record", camelToUnderline = false)
public class PaymentRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private String stripeSessionId;

    private String stripePaymentIntentId;

    private BigDecimal amount;

    private String currency;

    private String status;

    private String productType;

    private String description;

    private LocalDateTime refundTime;

    private String refundReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}