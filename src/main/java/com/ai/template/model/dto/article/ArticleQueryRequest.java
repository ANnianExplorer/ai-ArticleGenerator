package com.ai.template.model.dto.article;

import com.ai.template.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@EqualsAndHashCode(callSuper = true)
@Data
public class ArticleQueryRequest extends PageRequest implements Serializable {

    
    private Long userId;

    
    private String status;

    private static final long serialVersionUID = 1L;
}
