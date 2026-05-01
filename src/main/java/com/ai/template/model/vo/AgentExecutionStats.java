package com.ai.template.model.vo;

import com.ai.template.model.entity.AgentLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;

    private Integer totalDurationMs;

    private Integer agentCount;

    private Map<String, Integer> agentDurations;

    private String overallStatus;

    private List<AgentLog> logs;
}