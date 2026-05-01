package com.ai.template.model.vo;

import com.google.gson.reflect.TypeToken;
import com.ai.template.model.entity.Article;
import com.ai.template.utils.GsonUtils;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVO implements Serializable {

    private Long id;

    private String taskId;

    private Long userId;

    private String topic;

    private String userDescription;

    private String mainTitle;

    private String subTitle;

    private List<TitleOption> titleOptions;

    private List<OutlineItem> outline;

    private String content;

    private String fullContent;

    private String coverImage;

    private List<ImageItem> images;

    private String status;

    private String phase;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime completedTime;

    @Data
    public static class TitleOption implements Serializable {
        private String mainTitle;
        private String subTitle;
    }

    @Data
    public static class OutlineItem implements Serializable {
        private Integer section;
        private String title;
        private List<String> points;
    }

    @Data
    public static class ImageItem implements Serializable {
        private Integer position;
        private String url;
        private String method;
        private String keywords;
        private String sectionTitle;
        private String description;
    }

    public static ArticleVO objToVo(Article article) {
        if (article == null) {
            return null;
        }
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);
        
        if (article.getTitleOptions() != null) {
            articleVO.setTitleOptions(GsonUtils.fromJson(article.getTitleOptions(), 
                new TypeToken<List<TitleOption>>(){}));
        }
        if (article.getOutline() != null) {
            articleVO.setOutline(GsonUtils.fromJson(article.getOutline(), 
                new TypeToken<List<OutlineItem>>(){}));
        }
        if (article.getImages() != null) {
            articleVO.setImages(GsonUtils.fromJson(article.getImages(), 
                new TypeToken<List<ImageItem>>(){}));
        }
        
        return articleVO;
    }

    private static final long serialVersionUID = 1L;
}