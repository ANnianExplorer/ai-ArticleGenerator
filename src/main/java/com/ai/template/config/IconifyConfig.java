package com.ai.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "iconify")
@Data
public class IconifyConfig {

    
    private String apiUrl = "https://api.iconify.design";

    
    private Integer searchLimit = 10;

    
    private Integer defaultHeight = 64;

    
    private String defaultColor = "";
}