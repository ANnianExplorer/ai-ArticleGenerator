package com.ai.template.service;

import cn.hutool.core.util.StrUtil;
import com.ai.template.config.EmojiPackConfig;
import com.ai.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.ai.template.constant.ArticleConstant.PICSUM_URL_TEMPLATE;


@Service
@Slf4j
public class EmojiPackService implements ImageSearchService {

    @Resource
    private EmojiPackConfig emojiPackConfig;

    @Override
    public String searchImage(String keywords) {
        if (StrUtil.isBlank(keywords)) {
            log.warn("表情包搜索关键词为空");
            return null;
        }

        try {

            log.info("表情包搜�? {} -> {}", keywords, searchText);

            String fetchUrl = buildSearchUrl(searchText);

            Document document = Jsoup.connect(fetchUrl)
                    .timeout(emojiPackConfig.getTimeout())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            Element div = document.getElementsByClass("dgControl").first();
            if (div == null) {
                log.warn("Bing 未找到图片容�? keywords={}", keywords);
                return null;
            }

            if (imgElements.isEmpty()) {
                log.warn("Bing 未检索到表情�? keywords={}, searchText={}", keywords, searchText);
                return null;
            }

            String imageUrl = imgElements.get(0).attr("src");
            if (StrUtil.isBlank(imageUrl)) {
                log.warn("图片 URL 为空, keywords={}", keywords);
                return null;
            }


            log.info("表情包检索成�? {} -> {}", keywords, imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("表情包检索异�? keywords={}", keywords, e);
            return null;
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.EMOJI_PACK;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }

    
    private String buildSearchUrl(String searchText) {
        String encodedText = URLEncoder.encode(searchText, StandardCharsets.UTF_8);

        return String.format("%s?q=%s&mmasync=1", 
                emojiPackConfig.getSearchUrl(), 
                encodedText);
    }

    
    private String cleanImageUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return url;
        }
        
        int questionMarkIndex = url.indexOf("?");
        if (questionMarkIndex > 0) {
            return url.substring(0, questionMarkIndex);
        }
        
        return url;
    }
}
