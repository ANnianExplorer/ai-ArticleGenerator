package com.ai.template.model.enums;

import lombok.Getter;

@Getter
public enum SseMessageTypeEnum {

    AGENT1_COMPLETE("AGENT1_COMPLETE", "标题方案生成完成"),
    
    TITLES_GENERATED("TITLES_GENERATED", "标题方案已生�?),

    AGENT2_STREAMING("AGENT2_STREAMING", "大纲流式输出"),

    AGENT2_COMPLETE("AGENT2_COMPLETE", "大纲生成完成"),
    
    OUTLINE_GENERATED("OUTLINE_GENERATED", "大纲已生�?),

    AGENT3_STREAMING("AGENT3_STREAMING", "正文流式输出"),

    AGENT3_COMPLETE("AGENT3_COMPLETE", "正文生成完成"),

    AGENT4_COMPLETE("AGENT4_COMPLETE", "配图需求分析完�?),

    IMAGE_COMPLETE("IMAGE_COMPLETE", "单张配图完成"),

    AGENT5_COMPLETE("AGENT5_COMPLETE", "配图生成完成"),

    MERGE_COMPLETE("MERGE_COMPLETE", "图文合成完成"),

    ALL_COMPLETE("ALL_COMPLETE", "全部完成"),

    ERROR("ERROR", "错误");

    private final String value;

    private final String description;

    SseMessageTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getStreamingPrefix() {
        return this.value + ":";
    }
}