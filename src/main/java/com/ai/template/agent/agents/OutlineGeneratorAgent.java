package com.ai.template.agent.agents;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.ai.template.agent.context.StreamHandlerContext;
import com.ai.template.constant.PromptConstant;
import com.ai.template.model.dto.article.ArticleState;
import com.ai.template.model.enums.ArticleStyleEnum;
import com.ai.template.model.enums.SseMessageTypeEnum;
import com.ai.template.utils.GsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutlineGeneratorAgent implements NodeAction {

    private final DashScopeChatModel chatModel;

    public static final String INPUT_MAIN_TITLE = "mainTitle";
    public static final String INPUT_SUB_TITLE = "subTitle";
    public static final String INPUT_USER_DESCRIPTION = "userDescription";
    public static final String INPUT_STYLE = "style";
    public static final String OUTPUT_OUTLINE = "outline";

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String mainTitle = state.value(INPUT_MAIN_TITLE)
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("缺少主标题参�?));
        
        String subTitle = state.value(INPUT_SUB_TITLE)
                .map(Object::toString)
                .orElse("");
        
        String userDescription = state.value(INPUT_USER_DESCRIPTION)
                .map(Object::toString)
                .orElse(null);
        
        String style = state.value(INPUT_STYLE)
                .map(Object::toString)
                .orElse(null);
        
        log.info("OutlineGeneratorAgent 开始执�? mainTitle={}, subTitle={}", mainTitle, subTitle);

        String descriptionSection = "";
        if (userDescription != null && !userDescription.trim().isEmpty()) {
            descriptionSection = PromptConstant.AGENT2_DESCRIPTION_SECTION
                    .replace("{userDescription}", userDescription);
        }

        String prompt = PromptConstant.AGENT2_OUTLINE_PROMPT
                .replace("{mainTitle}", mainTitle)
                .replace("{subTitle}", subTitle)
                .replace("{descriptionSection}", descriptionSection)
                + getStylePrompt(style);


        String content = callLlmWithStreaming(prompt, streamHandler);

        ArticleState.OutlineResult outlineResult = GsonUtils.fromJson(
                content, 
                ArticleState.OutlineResult.class
        );
        
        log.info("OutlineGeneratorAgent 执行完成: 生成�?{} 个章�?, 
                outlineResult.getSections().size());
        
        return Map.of(OUTPUT_OUTLINE, outlineResult);
    }

    
    private String callLlmWithStreaming(String prompt, Consumer<String> streamHandler) {
        StringBuilder contentBuilder = new StringBuilder();
        
        Flux<ChatResponse> streamResponse = chatModel.stream(new Prompt(new UserMessage(prompt)));
        
        streamResponse
                .doOnNext(response -> {
                    String chunk = response.getResult().getOutput().getText();
                    if (chunk != null && !chunk.isEmpty()) {
                        contentBuilder.append(chunk);

                            streamHandler.accept(SseMessageTypeEnum.AGENT2_STREAMING.getStreamingPrefix() + chunk);
                        }
                    }
                })
                .doOnError(error -> log.error("OutlineGeneratorAgent 流式调用失败", error))
                .blockLast();
        
        return contentBuilder.toString();
    }

    
    private String getStylePrompt(String style) {
        if (style == null || style.isEmpty()) {
            return "";
        }
        
        ArticleStyleEnum styleEnum = ArticleStyleEnum.getEnumByValue(style);
        if (styleEnum == null) {
            return "";
        }
        
        return switch (styleEnum) {
            case TECH -> PromptConstant.STYLE_TECH_PROMPT;
            case EMOTIONAL -> PromptConstant.STYLE_EMOTIONAL_PROMPT;
            case EDUCATIONAL -> PromptConstant.STYLE_EDUCATIONAL_PROMPT;
            case HUMOROUS -> PromptConstant.STYLE_HUMOROUS_PROMPT;
        };
    }
}
