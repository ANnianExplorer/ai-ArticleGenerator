package com.ai.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "nano-banana")
@Data
public class NanoBananaConfig {

    private String apiKey;

    private String model = "gemini-2.5-flash-image";

    private String aspectRatio = "16:9";

    private String imageSize = "1K";

    private String outputMimeType = "image/png";
}