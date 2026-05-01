package com.ai.template.model.dto.image;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageRequest {

    private String keywords;

    private String prompt;

    private Integer position;

    private String type;

    private String aspectRatio;

    private String style;

    public String getEffectiveParam(boolean isAiGenerated) {
        if (isAiGenerated) {
            return prompt != null && !prompt.isEmpty() ? prompt : keywords;
        }
        return keywords != null && !keywords.isEmpty() ? keywords : prompt;
    }
}