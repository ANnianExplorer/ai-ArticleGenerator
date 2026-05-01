package com.ai.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.ai.template.constant.ArticleConstant.*;


@Configuration
@ConfigurationProperties(prefix = "emoji-pack")
@Data
public class EmojiPackConfig {

    
    private String searchUrl = BING_IMAGE_SEARCH_URL;

    
    private String suffix = EMOJI_PACK_SUFFIX;

    
    private Integer timeout = 10000;
}