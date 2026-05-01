package com.ai.template.service;

import com.mybatisflex.core.service.IService;
import com.ai.template.model.entity.AgentLog;
import com.ai.template.model.vo.AgentExecutionStats;

import java.util.List;

public interface AgentLogService extends IService<AgentLog> {

    void saveLogAsync(AgentLog log);

    List<AgentLog> getLogsByTaskId(String taskId);

    AgentExecutionStats getExecutionStats(String taskId);
}
