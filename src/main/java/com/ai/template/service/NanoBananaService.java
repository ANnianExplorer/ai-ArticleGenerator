package com.ai.template.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ImageConfig;
import com.google.genai.types.Part;
import com.ai.template.config.NanoBananaConfig;
import com.ai.template.model.dto.image.ImageData;
import com.ai.template.model.dto.image.ImageRequest;
import com.ai.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import static com.ai.template.constant.ArticleConstant.PICSUM_URL_TEMPLATE;


@Service
@Slf4j
public class NanoBananaService implements ImageSearchService {

    @Resource
    private NanoBananaConfig nanoBananaConfig;

    @Override
    public String searchImage(String keywords) {


        return null;
    }

    @Override
    public ImageData getImageData(ImageRequest request) {
        String prompt = request.getEffectiveParam(true);
        return generateImageData(prompt);
    }

    
    public ImageData generateImageData(String prompt) {
        try {

            Client genaiClient = Client.builder()
                    .apiKey(nanoBananaConfig.getApiKey())
                    .build();
            
            try {

                ImageConfig.Builder imageConfigBuilder = ImageConfig.builder()
                        .aspectRatio(nanoBananaConfig.getAspectRatio());

                if (model != null && model.contains("gemini-3-pro")) {
                    imageConfigBuilder.imageSize(nanoBananaConfig.getImageSize());
                }

                GenerateContentConfig config = GenerateContentConfig.builder()
                        .responseModalities("TEXT", "IMAGE")
                        .imageConfig(imageConfigBuilder.build())
                        .build();

                log.info("Nano Banana 开始生成图�? model={}, prompt={}", model, prompt);

                GenerateContentResponse response = genaiClient.models.generateContent(
                        model != null ? model : "gemini-2.5-flash-image",
                        prompt,
                        config);

                if (response.parts() != null) {
                    for (Part part : response.parts()) {
                        if (part.inlineData().isPresent()) {
                            var blob = part.inlineData().get();
                            if (blob.data().isPresent()) {
                                byte[] imageBytes = blob.data().get();
                                String mimeType = blob.mimeType().orElse("image/png");
                                
                                log.info("Nano Banana 图片生成成功, size={} bytes, mimeType={}", 
                                        imageBytes.length, mimeType);
                                
                                return ImageData.fromBytes(imageBytes, mimeType);
                            }
                        }
                    }
                }

                log.warn("Nano Banana 未生成图�? prompt={}", prompt);
                return null;

            } finally {
                genaiClient.close();
            }
        } catch (Exception e) {
            log.error("Nano Banana 生成图片异常, prompt={}", prompt, e);
            return null;
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.NANO_BANANA;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }
}
