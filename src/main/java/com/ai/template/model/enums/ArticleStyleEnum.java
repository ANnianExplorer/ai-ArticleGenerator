package com.ai.template.model.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum ArticleStyleEnum {

    TECH("tech", "科技风格"),
    EMOTIONAL("emotional", "情感风格"),
    EDUCATIONAL("educational", "教育风格"),
    HUMOROUS("humorous", "轻松幽默风格");

    private final String value;
    private final String text;

    ArticleStyleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public static List<String> getValues() {
        return Arrays.stream(values())
                .map(ArticleStyleEnum::getValue)
                .collect(Collectors.toList());
    }

    public static ArticleStyleEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (ArticleStyleEnum styleEnum : ArticleStyleEnum.values()) {
            if (styleEnum.getValue().equals(value)) {
                return styleEnum;
            }
        }
        return null;
    }

    public static boolean isValid(String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return getEnumByValue(value) != null;
    }
}