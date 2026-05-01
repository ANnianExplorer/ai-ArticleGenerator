package com.ai.template.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.system.SystemUtil;
import com.ai.template.config.MermaidConfig;
import com.ai.template.model.dto.image.ImageData;
import com.ai.template.model.dto.image.ImageRequest;
import com.ai.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.File;

import static com.ai.template.constant.ArticleConstant.PICSUM_URL_TEMPLATE;


@Service
@Slf4j
public class MermaidService implements ImageSearchService {

    @Resource
    private MermaidConfig mermaidConfig;

    @Override
    public String searchImage(String keywords) {


        ImageData imageData = generateDiagramData(keywords);

        return null;
    }

    @Override
    public String getImage(ImageRequest request) {


        return null;
    }

    @Override
    public ImageData getImageData(ImageRequest request) {

        String mermaidCode = request.getEffectiveParam(true);
        return generateDiagramData(mermaidCode);
    }

    
    public ImageData generateDiagramData(String mermaidCode) {
        if (mermaidCode == null || mermaidCode.trim().isEmpty()) {
            log.warn("Mermaid 代码为空");
            return null;
        }

        File tempInputFile = null;
        File tempOutputFile = null;

        try {

            tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
            FileUtil.writeUtf8String(mermaidCode, tempInputFile);

            String outputExtension = "." + mermaidConfig.getOutputFormat();
            tempOutputFile = FileUtil.createTempFile("mermaid_output_", outputExtension, true);


                log.error("Mermaid CLI 执行失败，输出文件不存在或为�?);
                return null;
            }

            byte[] imageBytes = FileUtil.readBytes(tempOutputFile);
            String mimeType = getMimeType(mermaidConfig.getOutputFormat());
            
            log.info("Mermaid 图表生成成功, size={} bytes", imageBytes.length);
            return ImageData.fromBytes(imageBytes, mimeType);

        } catch (Exception e) {
            log.error("Mermaid 图表生成异常", e);
            return null;
        } finally {

            if (tempInputFile != null) {
                FileUtil.del(tempInputFile);
            }
            if (tempOutputFile != null) {
                FileUtil.del(tempOutputFile);
            }
        }
    }

    
    private String getMimeType(String format) {
        return switch (format.toLowerCase()) {
            case "png" -> "image/png";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            default -> "image/png";
        };
    }

    
    private void convertMermaidToImage(File inputFile, File outputFile) {
        try {

            String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : mermaidConfig.getCliCommand();

                    command,
                    inputFile.getAbsolutePath(),
                    outputFile.getAbsolutePath(),
                    mermaidConfig.getBackgroundColor()
            );

            if (mermaidConfig.getWidth() != null && mermaidConfig.getWidth() > 0) {
                cmdLine += " -w " + mermaidConfig.getWidth();
            }

            log.info("执行 Mermaid CLI 命令: {}", cmdLine);

            
            log.debug("Mermaid CLI 执行结果: {}", result);

        } catch (Exception e) {
            log.error("执行 Mermaid CLI 失败", e);
            throw new RuntimeException("Mermaid CLI 执行失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.MERMAID;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }

    @Override
    public boolean isAvailable() {
        try {

            String checkCmd = command + " --version";
            String version = RuntimeUtil.execForStr(checkCmd);
            log.info("Mermaid CLI 版本: {}", version);
            return version != null && !version.isEmpty();
        } catch (Exception e) {
            log.warn("Mermaid CLI 不可�? {}", e.getMessage());
            return false;
        }
    }
}
