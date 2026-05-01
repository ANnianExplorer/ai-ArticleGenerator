package com.ai.template.model.dto.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer position;

    private String url;

    private String type;

    private String method;

    private String keywords;

    private String prompt;

    private String sectionTitle;
}