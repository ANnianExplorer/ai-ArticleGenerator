package com.ai.template.agent.parallel;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.ai.template.agent.context.StreamHandlerContext;
import com.ai.template.agent.tools.ImageGenerationTool;
import com.ai.template.model.dto.article.ArticleState;
import com.ai.template.model.enums.SseMessageTypeEnum;
import com.ai.template.utils.GsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Component
@Slf4j
@RequiredArgsConstructor
public class ParallelImageGenerator implements NodeAction {

    private final ImageGenerationTool imageGenerationTool;

    public static final String INPUT_IMAGE_REQUIREMENTS = "imageRequirements";
    public static final String OUTPUT_IMAGES = "images";

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        @SuppressWarnings("unchecked")
        List<ArticleState.ImageRequirement> imageRequirements = state.value(INPUT_IMAGE_REQUIREMENTS)
                .map(v -> {
                    if (v instanceof List) {
                        List<?> list = (List<?>) v;
                        if (list.isEmpty()) {
                            return new ArrayList<ArticleState.ImageRequirement>();
                        }
                        if (list.get(0) instanceof ArticleState.ImageRequirement) {
                            return (List<ArticleState.ImageRequirement>) v;
                        }

                        return convertToImageRequirements(list);
                    }
                    return new ArrayList<ArticleState.ImageRequirement>();
                })
                .orElse(new ArrayList<>());

        
        log.info("ParallelImageGenerator 开始执�? 配图需求数�?{}", imageRequirements.size());
        
        if (imageRequirements.isEmpty()) {
            log.info("没有配图需求，跳过图片生成");
            return Map.of(OUTPUT_IMAGES, new ArrayList<>());
        }

        Map<String, List<ArticleState.ImageRequirement>> groupedBySource = imageRequirements.stream()
                .collect(Collectors.groupingBy(ArticleState.ImageRequirement::getImageSource));
        
        log.info("配图需求按类型分组: {}", 
                groupedBySource.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().size()
                        )));


        allImages.sort((a, b) -> {
            Integer posA = a.getPosition() != null ? a.getPosition() : 0;
            Integer posB = b.getPosition() != null ? b.getPosition() : 0;
            return posA.compareTo(posB);
        });
        
        log.info("ParallelImageGenerator 执行完成: 成功生成 {} 张图�?, allImages.size());
        
        return Map.of(OUTPUT_IMAGES, allImages);
    }

    
    private List<ArticleState.ImageResult> executeParallel(
            Map<String, List<ArticleState.ImageRequirement>> groupedBySource,
            Consumer<String> streamHandler) {


        List<CompletableFuture<Void>> futures = groupedBySource.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() -> {
                    String imageSource = entry.getKey();
                    List<ArticleState.ImageRequirement> requirements = entry.getValue();
                    
                    log.info("开始处�?{} 类型的图片，数量: {}", imageSource, requirements.size());

                    for (ArticleState.ImageRequirement req : requirements) {
                        try {
                            ImageGenerationTool.ImageGenerationResult result = 
                                    imageGenerationTool.generateImageDirect(
                                            req.getImageSource(),
                                            req.getKeywords(),
                                            req.getPrompt(),
                                            req.getPosition(),
                                            req.getType(),
                                            req.getSectionTitle(),
                                            req.getPlaceholderId()
                                    );
                            
                            if (result.isSuccess()) {
                                ArticleState.ImageResult imageResult = convertToImageResult(result);
                                allImages.add(imageResult);

                                    String message = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix() 
                                            + GsonUtils.toJson(imageResult);
                                    streamHandler.accept(message);
                                }
                                
                                log.info("图片生成成功: imageSource={}, position={}", 
                                        imageSource, req.getPosition());
                            } else {
                                log.warn("图片生成失败: imageSource={}, position={}, error={}", 
                                        imageSource, req.getPosition(), result.getError());
                            }
                        } catch (Exception e) {
                            log.error("图片生成异常: imageSource={}, position={}", 
                                    imageSource, req.getPosition(), e);
                        }
                    }
                    
                    log.info("完成处理 {} 类型的图�?, imageSource);
                }))
                .toList();

        
        return new ArrayList<>(allImages);
    }

    
    private ArticleState.ImageResult convertToImageResult(ImageGenerationTool.ImageGenerationResult genResult) {
        ArticleState.ImageResult imageResult = new ArticleState.ImageResult();
        imageResult.setPosition(genResult.getPosition());
        imageResult.setUrl(genResult.getUrl());
        imageResult.setMethod(genResult.getMethod());
        imageResult.setKeywords(genResult.getKeywords());
        imageResult.setSectionTitle(genResult.getSectionTitle());
        imageResult.setDescription(genResult.getDescription());
        imageResult.setPlaceholderId(genResult.getPlaceholderId());
        return imageResult;
    }

    
    private List<ArticleState.ImageRequirement> convertToImageRequirements(List<?> list) {
        List<ArticleState.ImageRequirement> results = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof ArticleState.ImageRequirement) {
                results.add((ArticleState.ImageRequirement) item);
            } else if (item instanceof Map) {
                String json = GsonUtils.toJson(item);
                ArticleState.ImageRequirement req = GsonUtils.fromJson(json, ArticleState.ImageRequirement.class);
                results.add(req);
            }
        }
        return results;
    }
}
