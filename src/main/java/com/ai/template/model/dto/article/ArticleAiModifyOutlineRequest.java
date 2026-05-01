package com.ai.template.model.dto.article;

import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleAiModifyOutlineRequest implements Serializable {

    private String taskId;

    private String modifySuggestion;

    private static final long serialVersionUID = 1L;
}