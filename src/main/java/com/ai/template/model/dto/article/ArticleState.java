package com.ai.template.model.dto.article;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class ArticleState implements Serializable {

    
    private String taskId;

    
    private String topic;

    
    private String userDescription;

    
    private String style;

    
    private String phase;

    
    private List<TitleOption> titleOptions;

    
    private TitleResult title;

    
    private OutlineResult outline;

    
    private String content;

    
    private List<ImageRequirement> imageRequirements;

    
    private String coverImage;

    
    private List<ImageResult> images;

    
    private List<String> enabledImageMethods;

    
    @Data
    public static class TitleOption implements Serializable {
        private String mainTitle;
        private String subTitle;
    }

    
    @Data
    public static class TitleResult implements Serializable {
        private String mainTitle;
        private String subTitle;
    }

    
    @Data
    public static class OutlineResult implements Serializable {
        private List<OutlineSection> sections;
    }

    
    @Data
    public static class OutlineSection implements Serializable {
        private Integer section;
        private String title;
        private List<String> points;
    }

    
    @Data
    public static class ImageRequirement implements Serializable {
        private Integer position;
        private String type;
        private String sectionTitle;
        private String keywords;
        
        private String imageSource;
        
        private String prompt;
        
        private String placeholderId;
    }

    
    @Data
    public static class ImageResult implements Serializable {
        private Integer position;
        private String url;
        private String method;
        private String keywords;
        private String sectionTitle;
        private String description;
        
        private String placeholderId;
    }

    
    @Data
    public static class Agent4Result implements Serializable {
        
        private String contentWithPlaceholders;
        
        private List<ImageRequirement> imageRequirements;
    }

    
    private String fullContent;

    private static final long serialVersionUID = 1L;
}
