package com.ai.template.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "article", camelToUnderline = false)
public class Article implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String taskId;

    private Long userId;

    private String topic;

    private String userDescription;

    private String enabledImageMethods;

    private String style;

    private String mainTitle;

    private String subTitle;

    private String titleOptions;

    private String outline;

    private String content;

    private String fullContent;

    private String coverImage;

    private String images;

    private String status;

    private String phase;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime completedTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Integer isDelete;

}