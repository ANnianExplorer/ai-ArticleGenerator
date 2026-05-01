package com.ai.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "tencent.cos")
@Data
public class CosConfig {

    
    private String secretId;

    
    private String secretKey;

    
    private String region;

    
    private String bucket;
}