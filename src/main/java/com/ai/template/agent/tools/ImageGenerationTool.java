package com.ai.template.agent.tools;

import com.ai.template.model.dto.image.ImageRequest;
import com.ai.template.model.enums.ImageMethodEnum;
import com.ai.template.service.CosService;
import com.ai.template.service.ImageServiceStrategy;
import com.ai.template.utils.GsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.Serializable;


@Component
@Slf4j
public class ImageGenerationTool {

    @Resource
    private ImageServiceStrategy imageServiceStrategy;

    @Resource
    private CosService cosService;

    
    @Tool(description = "根据需求生成或搜索图片。支持多种图片来源：PEXELS（真实照片）、NANO_BANANA（AI生图）、MERMAID（流程图）、ICONIFY（图标）、EMOJI_PACK（表情包）、SVG_DIAGRAM（概念示意图�?)
    public String generateImage(
            @ToolParam(description = "图片来源类型：PEXELS/NANO_BANANA/MERMAID/ICONIFY/EMOJI_PACK/SVG_DIAGRAM") String imageSource,
            @ToolParam(description = "搜索关键词（用于 PEXELS/ICONIFY/EMOJI_PACK�?) String keywords,
            @ToolParam(description = "AI 生图提示词或图表代码（用�?NANO_BANANA/MERMAID/SVG_DIAGRAM�?) String prompt,
            @ToolParam(description = "图片位置序号，封面为1，其他为章节顺序") Integer position,
            @ToolParam(description = "图片类型：cover（封面）�?section（章节配图）") String type,
            @ToolParam(description = "对应的章节标题，封面图留�?) String sectionTitle) {
        
        log.info("ImageGenerationTool 开始执�? imageSource={}, position={}, type={}", 
                imageSource, position, type);
        
        try {

            ImageRequest imageRequest = ImageRequest.builder()
                    .keywords(keywords)
                    .prompt(prompt)
                    .position(position)
                    .type(type)
                    .build();

            String cosUrl = result.getUrl();
            ImageMethodEnum method = result.getMethod();

            ImageGenerationResult generationResult = new ImageGenerationResult();
            generationResult.setPosition(position);
            generationResult.setUrl(cosUrl);
            generationResult.setMethod(method.getValue());
            generationResult.setKeywords(keywords);
            generationResult.setSectionTitle(sectionTitle);
            generationResult.setDescription(type);
            generationResult.setSuccess(true);
            
            log.info("ImageGenerationTool 执行成功: position={}, method={}, cosUrl={}", 
                    position, method.getValue(), cosUrl);
            
            return GsonUtils.toJson(generationResult);
            
        } catch (Exception e) {
            log.error("ImageGenerationTool 执行失败: imageSource={}, position={}", imageSource, position, e);

            ImageGenerationResult failResult = new ImageGenerationResult();
            failResult.setPosition(position);
            failResult.setSuccess(false);
            failResult.setError(e.getMessage());
            failResult.setSectionTitle(sectionTitle);
            
            return GsonUtils.toJson(failResult);
        }
    }

    
    public ImageGenerationResult generateImageDirect(String imageSource, String keywords, String prompt,
                                                      Integer position, String type, String sectionTitle,
                                                      String placeholderId) {
        try {
            ImageRequest imageRequest = ImageRequest.builder()
                    .keywords(keywords)
                    .prompt(prompt)
                    .position(position)
                    .type(type)
                    .build();

            String cosUrl = result.getUrl();
            ImageMethodEnum method = result.getMethod();
            
            ImageGenerationResult generationResult = new ImageGenerationResult();
            generationResult.setPosition(position);
            generationResult.setUrl(cosUrl);
            generationResult.setMethod(method.getValue());
            generationResult.setKeywords(keywords);
            generationResult.setSectionTitle(sectionTitle);
            generationResult.setDescription(type);
            generationResult.setPlaceholderId(placeholderId);
            generationResult.setSuccess(true);
            
            return generationResult;
            
        } catch (Exception e) {
            log.error("图片生成失败: imageSource={}, position={}", imageSource, position, e);
            
            ImageGenerationResult failResult = new ImageGenerationResult();
            failResult.setPosition(position);
            failResult.setSuccess(false);
            failResult.setError(e.getMessage());
            failResult.setSectionTitle(sectionTitle);
            failResult.setPlaceholderId(placeholderId);
            
            return failResult;
        }
    }

    
    @Data
    public static class ImageGenerationResult implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Integer position;
        private String url;
        private String method;
        private String keywords;
        private String sectionTitle;
        private String description;
        private String placeholderId;
        private boolean success;
        private String error;
    }
}
