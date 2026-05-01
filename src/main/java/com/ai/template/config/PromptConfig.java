package com.ai.template.config;

import com.ai.template.constant.PromptConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;


@Configuration
@ConfigurationProperties(prefix = "prompt")
@Data
public class PromptConfig {

    
    private String version = "1.0";

    
    private Map<String, String> templates = new HashMap<>();

    @PostConstruct
    public void init() {
        templates.putIfAbsent("agent1_title", PromptConstant.AGENT1_TITLE_PROMPT);
        templates.putIfAbsent("agent2_outline", PromptConstant.AGENT2_OUTLINE_PROMPT);
        templates.putIfAbsent("agent3_content", PromptConstant.AGENT3_CONTENT_PROMPT);
        templates.putIfAbsent("agent4_image", PromptConstant.AGENT4_IMAGE_REQUIREMENTS_PROMPT);
        templates.putIfAbsent("ai_modify_outline", PromptConstant.AI_MODIFY_OUTLINE_PROMPT);
    }

    
    public String getPrompt(String key) {
        return templates.getOrDefault(key, "");
    }
}