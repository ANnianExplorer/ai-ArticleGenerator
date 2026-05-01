package com.ai.template.model.dto.article;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ArticleConfirmOutlineRequest implements Serializable {

    private String taskId;

    private List<ArticleState.OutlineSection> outline;

    private static final long serialVersionUID = 1L;
}