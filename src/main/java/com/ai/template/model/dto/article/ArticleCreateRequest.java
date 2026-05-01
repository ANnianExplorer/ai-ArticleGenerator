package com.ai.template.model.dto.article;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class ArticleCreateRequest implements Serializable {

    
    private String topic;

    
    private String style;

    
    private List<String> enabledImageMethods;

    private static final long serialVersionUID = 1L;
}
