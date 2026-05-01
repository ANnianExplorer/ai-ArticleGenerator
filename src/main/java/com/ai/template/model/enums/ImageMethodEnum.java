package com.ai.template.model.enums;

import lombok.Getter;

@Getter
public enum ImageMethodEnum {

    PEXELS("PEXELS", "Pexels 图库", false, false),

    NANO_BANANA("NANO_BANANA", "Nano Banana AI 生图", true, false),

    MERMAID("MERMAID", "Mermaid 流程图生�?, true, false),

    ICONIFY("ICONIFY", "Iconify 图标�?, false, false),

    EMOJI_PACK("EMOJI_PACK", "表情包检�?, false, false),

    SVG_DIAGRAM("SVG_DIAGRAM", "SVG 概念示意�?, true, false),

    PICSUM("PICSUM", "Picsum 随机图片", false, true);

    private final String value;

    private final String description;

    private final boolean aiGenerated;

    private final boolean fallback;

    ImageMethodEnum(String value, String description, boolean aiGenerated, boolean fallback) {
        this.value = value;
        this.description = description;
        this.aiGenerated = aiGenerated;
        this.fallback = fallback;
    }

    public static ImageMethodEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ImageMethodEnum methodEnum : values()) {
            if (methodEnum.getValue().equals(value)) {
                return methodEnum;
            }
        }
        return null;
    }

    public static ImageMethodEnum getDefaultSearchMethod() {
        return PEXELS;
    }

    public static ImageMethodEnum getDefaultAiMethod() {
        return NANO_BANANA;
    }

    public static ImageMethodEnum getFallbackMethod() {
        return PICSUM;
    }
}