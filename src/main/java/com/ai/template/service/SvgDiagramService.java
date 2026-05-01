package com.ai.template.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.ai.template.config.SvgDiagramConfig;
import com.ai.template.constant.PromptConstant;
import com.ai.template.model.dto.image.ImageData;
import com.ai.template.model.dto.image.ImageRequest;
import com.ai.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;

import static com.ai.template.constant.ArticleConstant.PICSUM_URL_TEMPLATE;


@Service
@Slf4j
public class SvgDiagramService implements ImageSearchService {

    @Resource
    private SvgDiagramConfig svgDiagramConfig;

    @Resource
    private DashScopeChatModel chatModel;

    @Override
    public String searchImage(String keywords) {


        return null;
    }

    @Override
    public ImageData getImageData(ImageRequest request) {
        String requirement = request.getEffectiveParam(true);
        return generateSvgDiagramData(requirement);
    }

    
    public ImageData generateSvgDiagramData(String requirement) {
        if (StrUtil.isBlank(requirement)) {
            log.warn("SVG 图表需求为�?);
            return null;
        }

        try {

            String svgCode = callLlmToGenerateSvg(requirement);

            if (StrUtil.isBlank(svgCode)) {
                log.error("LLM 未生�?SVG 代码");
                return null;
            }

            if (!isValidSvg(svgCode)) {
                log.error("生成�?SVG 代码格式无效");
                return null;
            }

            
            log.info("SVG 概念示意图生成成�? size={} bytes", svgBytes.length);
            return ImageData.fromBytes(svgBytes, "image/svg+xml");

        } catch (Exception e) {
            log.error("SVG 概念示意图生成异�? requirement={}", requirement, e);
            return null;
        }
    }

    
    private String callLlmToGenerateSvg(String requirement) {
        String prompt = PromptConstant.SVG_DIAGRAM_GENERATION_PROMPT
                .replace("{requirement}", requirement);

        log.info("开始调�?LLM 生成 SVG 概念示意�?);

        ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
        String svgCode = response.getResult().getOutput().getText().trim();

        svgCode = extractSvgCode(svgCode);

        return svgCode;
    }

    
    private String extractSvgCode(String text) {
        if (text == null) {
            return null;
        }


        if (!text.startsWith("<?xml")) {

                text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + text;
            }
        }

        return text;
    }

    
    private boolean isValidSvg(String svgCode) {
        if (StrUtil.isBlank(svgCode)) {
            return false;
        }

        return svgCode.contains("<svg") && svgCode.contains("</svg>");
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.SVG_DIAGRAM;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }
}
