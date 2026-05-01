package com.ai.template.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ai.template.constant.ArticleConstant.SSE_RECONNECT_TIME_MS;
import static com.ai.template.constant.ArticleConstant.SSE_TIMEOUT_MS;


@Component
@Slf4j
public class SseEmitterManager {

    
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    
    public SseEmitter createEmitter(String taskId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时, taskId={}", taskId);
            emitterMap.remove(taskId);
        });

        emitter.onCompletion(() -> {
            log.info("SSE 连接完成, taskId={}", taskId);
            emitterMap.remove(taskId);
        });

        emitter.onError((e) -> {
            log.error("SSE 连接错误, taskId={}", taskId, e);
            emitterMap.remove(taskId);
        });
        
        emitterMap.put(taskId, emitter);
        log.info("SSE 连接已创�? taskId={}", taskId);
        
        return emitter;
    }

    
    public void send(String taskId, String message) {
        SseEmitter emitter = emitterMap.get(taskId);
        if (emitter == null) {
            log.warn("SSE Emitter 不存�? taskId={}", taskId);
            return;
        }
        
        try {
            emitter.send(SseEmitter.event()
                    .data(message)
                    .reconnectTime(SSE_RECONNECT_TIME_MS));
            log.debug("SSE 消息发送成�? taskId={}, message={}", taskId, message);
        } catch (IOException e) {
            log.error("SSE 消息发送失�? taskId={}", taskId, e);
            emitterMap.remove(taskId);
        }
    }

    
    public void complete(String taskId) {
        SseEmitter emitter = emitterMap.get(taskId);
        if (emitter == null) {
            log.warn("SSE Emitter 不存�? taskId={}", taskId);
            return;
        }
        
        try {
            emitter.complete();
            log.info("SSE 连接已完�? taskId={}", taskId);
        } catch (Exception e) {
            log.error("SSE 连接完成失败, taskId={}", taskId, e);
        } finally {
            emitterMap.remove(taskId);
        }
    }

    
    public boolean exists(String taskId) {
        return emitterMap.containsKey(taskId);
    }
}
