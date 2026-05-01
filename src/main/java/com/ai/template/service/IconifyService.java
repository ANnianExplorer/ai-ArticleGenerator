package com.ai.template.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ai.template.config.IconifyConfig;
import com.ai.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.ai.template.constant.ArticleConstant.PICSUM_URL_TEMPLATE;


@Service
@Slf4j
public class IconifyService implements ImageSearchService {

    @Resource
    private IconifyConfig iconifyConfig;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    public String searchImage(String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) {
            log.warn("Iconify 搜索关键词为�?);
            return null;
        }

        try {

            String searchUrl = buildSearchUrl(keywords);
            String searchResult = callApi(searchUrl);

            if (searchResult == null) {
                return null;
            }

            if (iconName == null) {
                log.warn("Iconify 未检索到图标: {}", keywords);
                return null;
            }

            String svgUrl = buildSvgUrl(iconName);
            log.info("Iconify 图标检索成�? {} -> {}", keywords, iconName);
            
            return svgUrl;

        } catch (Exception e) {
            log.error("Iconify 图标检索异�? keywords={}", keywords, e);
            return null;
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.ICONIFY;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }

    
    private String buildSearchUrl(String keywords) {
        String encodedKeywords = URLEncoder.encode(keywords, StandardCharsets.UTF_8);
        return String.format("%s/search?query=%s&limit=%d",
                iconifyConfig.getApiUrl(),
                encodedKeywords,
                iconifyConfig.getSearchLimit());
    }

    
    private String callApi(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Iconify API 调用失败: {}", response.code());
                    return null;
                }

                return response.body().string();
            }
        } catch (IOException e) {
            log.error("Iconify API 调用异常", e);
            return null;
        }
    }

    
    private String extractFirstIcon(String jsonResponse) {
        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray icons = json.getAsJsonArray("icons");

            if (icons == null || icons.isEmpty()) {
                return null;
            }

            return icons.get(0).getAsString();
        } catch (Exception e) {
            log.error("解析 Iconify 搜索结果失败", e);
            return null;
        }
    }

    
    private String buildSvgUrl(String iconName) {

        String path = iconName.replace(":", "/");

        StringBuilder url = new StringBuilder(iconifyConfig.getApiUrl())
                .append("/")
                .append(path)
                .append(".svg");

        boolean hasParams = false;
        if (iconifyConfig.getDefaultHeight() != null && iconifyConfig.getDefaultHeight() > 0) {
            url.append("?height=").append(iconifyConfig.getDefaultHeight());
            hasParams = true;
        }

            url.append(hasParams ? "&" : "?");

            if (color.startsWith("#")) {
                color = "%23" + color.substring(1);
            }
            url.append("color=").append(color);
        }

        return url.toString();
    }
}
