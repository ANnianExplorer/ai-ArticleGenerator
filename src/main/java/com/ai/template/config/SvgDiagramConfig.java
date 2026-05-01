package com.ai.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.ai.template.constant.ArticleConstant.*;


@Configuration
@ConfigurationProperties(prefix = "svg-diagram")
@Data
public class SvgDiagramConfig {

    
    private Integer defaultWidth = SVG_DEFAULT_WIDTH;

    
    private Integer defaultHeight = SVG_DEFAULT_HEIGHT;

    
    private String folder = "svg-diagrams";
}
