package com.ai.template.aop;

import com.ai.template.annotation.AgentExecution;
import com.ai.template.model.dto.article.ArticleState;
import com.ai.template.model.entity.AgentLog;
import com.ai.template.service.AgentLogService;
import com.ai.template.utils.GsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Aspect
@Component
@Slf4j
public class AgentExecutionAspect {

    @Resource
    private AgentLogService agentLogService;

    @Around("@annotation(agentExecution)")
    public Object aroundAgentExecution(ProceedingJoinPoint pjp, AgentExecution agentExecution) throws Throwable {
        long startTime = System.currentTimeMillis();
        LocalDateTime startDateTime = LocalDateTime.now();
        
        String taskId = extractTaskId(pjp);
        String inputData = extractInputData(pjp);
        String prompt = extractPrompt(pjp);
        
        AgentLog agentLog = AgentLog.builder()
                .taskId(taskId)
                .agentName(agentExecution.value())
                .startTime(startDateTime)
                .status("RUNNING")
                .prompt(prompt)
                .inputData(inputData)
                .build();

        Object result = null;
        try {
            result = pjp.proceed();
            
            agentLog.setStatus("SUCCESS");
            agentLog.setEndTime(LocalDateTime.now());
            agentLog.setDurationMs((int) (System.currentTimeMillis() - startTime));
            agentLog.setOutputData(extractOutputData(result));
            
            log.info("Agent execution success: {}, taskId={}, duration={}ms", 
                    agentExecution.value(), taskId, agentLog.getDurationMs());
            
        } catch (Throwable e) {
            agentLog.setStatus("FAILED");
            agentLog.setEndTime(LocalDateTime.now());
            agentLog.setDurationMs((int) (System.currentTimeMillis() - startTime));
            agentLog.setErrorMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            
            log.error("Agent execution failed: {}, taskId={}, error={}", 
                    agentExecution.value(), taskId, e.getMessage(), e);
            
            throw e;
        } finally {
            agentLogService.saveLogAsync(agentLog);
        }

        return result;
    }

    
    private String extractTaskId(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) {
            return "unknown";
        }

        for (Object arg : args) {
            if (arg instanceof ArticleState) {
                return ((ArticleState) arg).getTaskId();
            }
        }

        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            }
        }

        return "unknown";
    }

    
    private String extractInputData(ProceedingJoinPoint pjp) {
        try {
            Object[] args = pjp.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }

            Map<String, Object> inputMap = new HashMap<>();
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String[] paramNames = signature.getParameterNames();

            for (int i = 0; i < args.length && i < paramNames.length; i++) {
                Object arg = args[i];
                if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                    inputMap.put(paramNames[i], arg);
                } else if (arg instanceof ArticleState) {
                    ArticleState state = (ArticleState) arg;
                    inputMap.put("taskId", state.getTaskId());
                    if (state.getTitle() != null) {
                        inputMap.put("mainTitle", state.getTitle().getMainTitle());
                    }
                }
            }

            return inputMap.isEmpty() ? null : GsonUtils.toJson(inputMap);
        } catch (Exception e) {
            log.warn("Extract input data failed", e);
            return null;
        }
    }

    
    private String extractOutputData(Object result) {
        try {
            if (result == null) {
                return null;
            }

            if (result instanceof String || result instanceof Number || result instanceof Boolean) {
                return String.valueOf(result);
            }

            if (result instanceof java.util.List) {
                return "{\"listSize\": " + ((java.util.List<?>) result).size() + "}";
            }

            return "{\"type\": \"" + result.getClass().getSimpleName() + "\"}";
        } catch (Exception e) {
            log.warn("Extract output data failed", e);
            return null;
        }
    }

    
    private String extractPrompt(ProceedingJoinPoint pjp) {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        } catch (Exception e) {
            return null;
        }
    }
}