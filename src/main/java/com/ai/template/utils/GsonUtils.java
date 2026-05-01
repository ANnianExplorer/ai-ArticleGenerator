package com.ai.template.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;


@Slf4j
public class GsonUtils {

    
    private static final Gson GSON = new GsonBuilder()
            .create();

    private GsonUtils() {
    }

    
    public static Gson getInstance() {
        return GSON;
    }

    
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return GSON.toJson(obj);
    }

    
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, clazz);
    }

    
    public static <T> T fromJson(String json, TypeToken<T> typeToken) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, typeToken.getType());
    }

    
    public static <T> T fromJson(String json, Type type) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, type);
    }

    
    public static <T> T fromJsonSafe(String json, Class<T> clazz) {
        try {
            return fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            log.error("JSON parse failed, json={}", json, e);
            return null;
        }
    }

    
    public static <T> T fromJsonSafe(String json, TypeToken<T> typeToken) {
        try {
            return fromJson(json, typeToken);
        } catch (JsonSyntaxException e) {
            log.error("JSON parse failed, json={}", json, e);
            return null;
        }
    }
}