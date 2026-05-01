package com.ai.template.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ai.template.mapper.AgentLogMapper;
import com.ai.template.model.entity.AgentLog;
import com.ai.template.model.vo.AgentExecutionStats;
import com.ai.template.service.AgentLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AgentLogServiceImpl extends ServiceImpl<AgentLogMapper, AgentLog> implements AgentLogService {

    @Override
    @Async
    public void saveLogAsync(AgentLog agentLog) {
        try {
            this.save(agentLog);
            log.info("智能体日志已保存, taskId={}, agentName={}, status={}, durationMs={}", 
                    agentLog.getTaskId(), agentLog.getAgentName(), agentLog.getStatus(), agentLog.getDurationMs());
        } catch (Exception e) {
            log.error("保存智能体日志失�? taskId={}, agentName={}", 
                    agentLog.getTaskId(), agentLog.getAgentName(), e);
        }
    }

    @Override
    public List<AgentLog> getLogsByTaskId(String taskId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("taskId", taskId)
                .orderBy("createTime", true);
        return this.list(queryWrapper);
    }

    @Override
    public AgentExecutionStats getExecutionStats(String taskId) {
        List<AgentLog> logs = getLogsByTaskId(taskId);
        
        if (logs == null || logs.isEmpty()) {
            return AgentExecutionStats.builder()
                    .taskId(taskId)
                    .agentCount(0)
                    .totalDurationMs(0)
                    .overallStatus("NOT_FOUND")
                    .build();
        }

        int totalDuration = 0;
        Map<String, Integer> agentDurations = new HashMap<>();
        String overallStatus = "SUCCESS";

        for (AgentLog log : logs) {

            if (log.getDurationMs() != null) {
                totalDuration += log.getDurationMs();
                agentDurations.put(log.getAgentName(), log.getDurationMs());
            }

                overallStatus = "FAILED";
            } else if ("RUNNING".equals(log.getStatus()) && !"FAILED".equals(overallStatus)) {
                overallStatus = "RUNNING";
            }
        }

        return AgentExecutionStats.builder()
                .taskId(taskId)
                .totalDurationMs(totalDuration)
                .agentCount(logs.size())
                .agentDurations(agentDurations)
                .overallStatus(overallStatus)
                .logs(logs)
                .build();
    }
}
