package com.ai.template.model.dto.article;

import lombok.Data;

import java.io.Serializable;


@Data
public class ArticleConfirmTitleRequest implements Serializable {

    
    private String taskId;

    
    private String selectedMainTitle;

    
    private String selectedSubTitle;

    
    private String userDescription;

    private static final long serialVersionUID = 1L;
}
