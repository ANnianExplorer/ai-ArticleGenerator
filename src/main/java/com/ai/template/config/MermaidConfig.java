package com.ai.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mermaid")
@Data
public class MermaidConfig {

    private String cliCommand = "mmdc";

    private String backgroundColor = "transparent";

    private String outputFormat = "svg";

    private Integer width = 800;

    private Long timeout = 30000L;
}