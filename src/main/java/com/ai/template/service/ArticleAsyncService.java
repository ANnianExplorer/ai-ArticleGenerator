package com.ai.template.service;

import com.google.gson.reflect.TypeToken;
import com.ai.template.agent.ArticleAgentOrchestrator;
import com.ai.template.agent.config.AgentConfig;
import com.ai.template.manager.SseEmitterManager;
import com.ai.template.model.dto.article.ArticleState;
import com.ai.template.model.entity.Article;
import com.ai.template.model.enums.ArticlePhaseEnum;
import com.ai.template.model.enums.ArticleStatusEnum;
import com.ai.template.model.enums.SseMessageTypeEnum;
import com.ai.template.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ArticleAsyncService {

    @Resource
    private ArticleAgentService articleAgentService;

    @Resource
    private ArticleAgentOrchestrator articleAgentOrchestrator;

    @Resource
    private AgentConfig agentConfig;

    @Resource
    private SseEmitterManager sseEmitterManager;

    @Resource
    private ArticleService articleService;

    @Async("articleExecutor")
    public void executePhase1(String taskId, String topic, String style) {
        boolean useOrchestrator = agentConfig.isOrchestratorEnabled();
        log.info("阶段1异步任务开�? taskId={}, topic={}, style={}, 使用多智能体编排={}", 
                taskId, topic, style, useOrchestrator);
        
        try {

            articleService.updateArticleStatus(taskId, ArticleStatusEnum.PROCESSING, null);
            articleService.updatePhase(taskId, ArticlePhaseEnum.TITLE_GENERATING);

            state.setTaskId(taskId);
            state.setTopic(topic);
            state.setStyle(style);

                articleAgentOrchestrator.executePhase1_GenerateTitles(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            } else {
                articleAgentService.executePhase1_GenerateTitles(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            }

            articleService.saveTitleOptions(taskId, state.getTitleOptions());

            articleService.updatePhase(taskId, ArticlePhaseEnum.TITLE_SELECTING);

            data.put("titleOptions", state.getTitleOptions());
            sendSseMessage(taskId, SseMessageTypeEnum.TITLES_GENERATED, data);
            
            log.info("阶段1异步任务完成, taskId={}", taskId);
        } catch (Exception e) {
            log.error("阶段1异步任务失败, taskId={}", taskId, e);

            articleService.updateArticleStatus(taskId, ArticleStatusEnum.FAILED, e.getMessage());


            sseEmitterManager.complete(taskId);
        }
    }

    @Async("articleExecutor")
    public void executePhase2(String taskId) {
        boolean useOrchestrator = agentConfig.isOrchestratorEnabled();
        log.info("阶段2异步任务开�? taskId={}, 使用多智能体编排={}", taskId, useOrchestrator);
        
        try {

            Article article = articleService.getByTaskId(taskId);
            if (article == null) {
                throw new RuntimeException("文章不存�?);
            }

            state.setTaskId(taskId);
            state.setStyle(article.getStyle());
            state.setUserDescription(article.getUserDescription());

            ArticleState.TitleResult title = new ArticleState.TitleResult();
            title.setMainTitle(article.getMainTitle());
            title.setSubTitle(article.getSubTitle());
            state.setTitle(title);

                articleAgentOrchestrator.executePhase2_GenerateOutline(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            } else {
                articleAgentService.executePhase2_GenerateOutline(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            }

            Article articleToUpdate = articleService.getByTaskId(taskId);
            articleToUpdate.setOutline(GsonUtils.toJson(state.getOutline().getSections()));
            articleService.updateById(articleToUpdate);


            data.put("outline", state.getOutline().getSections());
            sendSseMessage(taskId, SseMessageTypeEnum.OUTLINE_GENERATED, data);
            
            log.info("阶段2异步任务完成, taskId={}", taskId);
        } catch (Exception e) {
            log.error("阶段2异步任务失败, taskId={}", taskId, e);

            articleService.updateArticleStatus(taskId, ArticleStatusEnum.FAILED, e.getMessage());


            sseEmitterManager.complete(taskId);
        }
    }

    
    @Async("articleExecutor")
    public void executePhase3(String taskId) {
        boolean useOrchestrator = agentConfig.isOrchestratorEnabled();
        log.info("阶段3异步任务开�? taskId={}, 使用多智能体编排={}", taskId, useOrchestrator);
        
        try {

            Article article = articleService.getByTaskId(taskId);
            if (article == null) {
                throw new RuntimeException("文章不存�?);
            }

            state.setTaskId(taskId);
            state.setStyle(article.getStyle());

            if (article.getEnabledImageMethods() != null) {
                enabledMethods = GsonUtils.fromJson(
                        article.getEnabledImageMethods(),
                        new TypeToken<List<String>>(){}
                );
            }
            state.setEnabledImageMethods(enabledMethods);

            ArticleState.TitleResult title = new ArticleState.TitleResult();
            title.setMainTitle(article.getMainTitle());
            title.setSubTitle(article.getSubTitle());
            state.setTitle(title);

            List<ArticleState.OutlineSection> outlineSections = GsonUtils.fromJson(
                    article.getOutline(),
                    new TypeToken<List<ArticleState.OutlineSection>>(){}
            );
            ArticleState.OutlineResult outlineResult = new ArticleState.OutlineResult();
            outlineResult.setSections(outlineSections);
            state.setOutline(outlineResult);

            if (useOrchestrator) {
                articleAgentOrchestrator.executePhase3_GenerateContent(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            } else {
                articleAgentService.executePhase3_GenerateContent(state, message -> {
                    handleAgentMessage(taskId, message, state);
                });
            }

            articleService.saveArticleContent(taskId, state);



            sseEmitterManager.complete(taskId);
            
            log.info("阶段3异步任务完成, taskId={}", taskId);
        } catch (Exception e) {
            log.error("阶段3异步任务失败, taskId={}", taskId, e);

            articleService.updateArticleStatus(taskId, ArticleStatusEnum.FAILED, e.getMessage());


            sseEmitterManager.complete(taskId);
        }
    }

    
    private void handleAgentMessage(String taskId, String message, ArticleState state) {
        Map<String, Object> data = buildMessageData(message, state);
        if (data != null) {
            sseEmitterManager.send(taskId, GsonUtils.toJson(data));
        }
    }

    
    private Map<String, Object> buildMessageData(String message, ArticleState state) {

        String streamingPrefix2 = SseMessageTypeEnum.AGENT2_STREAMING.getStreamingPrefix();
        String streamingPrefix3 = SseMessageTypeEnum.AGENT3_STREAMING.getStreamingPrefix();
        String imageCompletePrefix = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix();
        
        if (message.startsWith(streamingPrefix2)) {
            return buildStreamingData(SseMessageTypeEnum.AGENT2_STREAMING, message.substring(streamingPrefix2.length()));
        }
        
        if (message.startsWith(streamingPrefix3)) {
            return buildStreamingData(SseMessageTypeEnum.AGENT3_STREAMING, message.substring(streamingPrefix3.length()));
        }
        
        if (message.startsWith(imageCompletePrefix)) {
            String imageJson = message.substring(imageCompletePrefix.length());
            return buildImageCompleteData(imageJson);
        }

        return buildCompleteMessageData(message, state);
    }

    
    private Map<String, Object> buildStreamingData(SseMessageTypeEnum type, String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type.getValue());
        data.put("content", content);
        return data;
    }

    
    private Map<String, Object> buildImageCompleteData(String imageJson) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", SseMessageTypeEnum.IMAGE_COMPLETE.getValue());
        data.put("image", GsonUtils.fromJson(imageJson, ArticleState.ImageResult.class));
        return data;
    }

    
    private Map<String, Object> buildCompleteMessageData(String message, ArticleState state) {
        Map<String, Object> data = new HashMap<>();

            data.put("type", SseMessageTypeEnum.AGENT1_COMPLETE.getValue());
            data.put("title", state.getTitle());
        } else if (SseMessageTypeEnum.AGENT2_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT2_COMPLETE.getValue());
            data.put("outline", state.getOutline().getSections());
        } else if (SseMessageTypeEnum.AGENT3_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT3_COMPLETE.getValue());
        } else if (SseMessageTypeEnum.AGENT4_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT4_COMPLETE.getValue());
            data.put("imageRequirements", state.getImageRequirements());
        } else if (SseMessageTypeEnum.AGENT5_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT5_COMPLETE.getValue());
            data.put("images", state.getImages());
        } else if (SseMessageTypeEnum.MERGE_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.MERGE_COMPLETE.getValue());
            data.put("fullContent", state.getFullContent());
        } else {
            return null;
        }
        
        return data;
    }

    
    private void sendSseMessage(String taskId, SseMessageTypeEnum type, Map<String, Object> additionalData) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type.getValue());
        data.putAll(additionalData);
        sseEmitterManager.send(taskId, GsonUtils.toJson(data));
    }
}
