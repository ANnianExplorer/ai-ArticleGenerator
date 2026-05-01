package com.ai.template.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "stripe")
@Data
public class StripeConfig {

    
    private String apiKey;

    
    private String webhookSecret;

    
    private String successUrl;

    
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = this.apiKey;
    }
}
