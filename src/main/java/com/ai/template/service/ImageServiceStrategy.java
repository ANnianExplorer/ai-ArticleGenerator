package com.ai.template.service;

import com.ai.template.model.dto.image.ImageData;
import com.ai.template.model.dto.image.ImageRequest;
import com.ai.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ImageServiceStrategy {

    @Resource
    private List<ImageSearchService> imageSearchServices;

    @Resource
    private CosService cosService;

    
    private final Map<ImageMethodEnum, ImageSearchService> serviceMap = new EnumMap<>(ImageMethodEnum.class);

    @PostConstruct
    public void init() {

        for (ImageSearchService service : imageSearchServices) {
            ImageMethodEnum method = service.getMethod();
            serviceMap.put(method, service);
            log.info("注册图片服务: {} -> {} (AI生图: {}, 降级: {})", 
                    method.getValue(), 
                    service.getClass().getSimpleName(),
                    method.isAiGenerated(),
                    method.isFallback());
        }
    }

    
    public ImageResult getImageAndUpload(String imageSource, ImageRequest request) {
        ImageMethodEnum method = resolveMethod(imageSource);
        ImageSearchService service = serviceMap.get(method);
        
        if (service == null || !service.isAvailable()) {
            log.warn("图片服务不可�? {}, 尝试降级", method);
            return handleFallbackWithUpload(request.getPosition());
        }

        try {

            ImageData imageData = service.getImageData(request);
            
            if (imageData == null || !imageData.isValid()) {
                log.warn("图片数据获取失败, 使用降级方案, method={}", method);
                return handleFallbackWithUpload(request.getPosition());
            }

            String folder = getFolderForMethod(method);
            String cosUrl = cosService.uploadImageData(imageData, folder);
            
            if (cosUrl != null && !cosUrl.isEmpty()) {
                log.info("图片获取并上传成�? method={}, cosUrl={}", method, cosUrl);
                return new ImageResult(cosUrl, method);
            } else {
                log.warn("图片上传 COS 失败, 使用降级方案, method={}", method);
                return handleFallbackWithUpload(request.getPosition());
            }
        } catch (Exception e) {
            log.error("获取图片并上传异�? method={}", method, e);
            return handleFallbackWithUpload(request.getPosition());
        }
    }

    
    @Deprecated
    public ImageResult getImage(String imageSource, ImageRequest request) {
        ImageMethodEnum method = resolveMethod(imageSource);
        ImageSearchService service = serviceMap.get(method);
        
        if (service == null || !service.isAvailable()) {
            log.warn("图片服务不可�? {}, 尝试降级", method);
            return handleFallback(request.getPosition());
        }

        String imageUrl = service.getImage(request);
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return new ImageResult(imageUrl, method);
        } else {
            log.warn("图片获取失败, 使用降级方案, method={}", method);
            return handleFallback(request.getPosition());
        }
    }

    
    @Deprecated
    public ImageResult getImage(String imageSource, String keywords, String prompt) {
        ImageRequest request = ImageRequest.builder()
                .keywords(keywords)
                .prompt(prompt)
                .build();
        return getImage(imageSource, request);
    }

    
    private String getFolderForMethod(ImageMethodEnum method) {
        return switch (method) {
            case PEXELS -> "pexels";
            case NANO_BANANA -> "nano-banana";
            case MERMAID -> "mermaid";
            case ICONIFY -> "iconify";
            case EMOJI_PACK -> "emoji-pack";
            case SVG_DIAGRAM -> "svg-diagram";
            case PICSUM -> "picsum";
        };
    }

    
    private ImageMethodEnum resolveMethod(String imageSource) {
        ImageMethodEnum method = ImageMethodEnum.getByValue(imageSource);
        if (method == null) {
            log.warn("未知的图片来�? {}, 默认使用 {}", imageSource, ImageMethodEnum.getDefaultSearchMethod());
            return ImageMethodEnum.getDefaultSearchMethod();
        }
        return method;
    }

    
    private ImageResult handleFallback(Integer position) {
        int pos = position != null ? position : 1;
        String fallbackUrl = getFallbackImage(pos);
        return new ImageResult(fallbackUrl, ImageMethodEnum.getFallbackMethod());
    }

    
    private ImageResult handleFallbackWithUpload(Integer position) {
        int pos = position != null ? position : 1;
        String fallbackUrl = getFallbackImage(pos);

        ImageData fallbackData = ImageData.fromUrl(fallbackUrl);
        String cosUrl = cosService.uploadImageData(fallbackData, "fallback");

        String finalUrl = (cosUrl != null && !cosUrl.isEmpty()) ? cosUrl : fallbackUrl;
        return new ImageResult(finalUrl, ImageMethodEnum.getFallbackMethod());
    }

    
    public ImageSearchService getService(ImageMethodEnum method) {
        return serviceMap.get(method);
    }

    
    public String getFallbackImage(int position) {

        ImageSearchService defaultService = serviceMap.get(ImageMethodEnum.getDefaultSearchMethod());
        if (defaultService != null) {
            return defaultService.getFallbackImage(position);
        }
        return String.format("https://picsum.photos/800/600?random=%d", position);
    }

    
    public List<ImageMethodEnum> getRegisteredMethods() {
        return List.copyOf(serviceMap.keySet());
    }

    
    public static class ImageResult {
        private final String url;
        private final ImageMethodEnum method;

        public ImageResult(String url, ImageMethodEnum method) {
            this.url = url;
            this.method = method;
        }

        public String getUrl() {
            return url;
        }

        public ImageMethodEnum getMethod() {
            return method;
        }

        public boolean isSuccess() {
            return url != null && !url.isEmpty();
        }
    }
}
