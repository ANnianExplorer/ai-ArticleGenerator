package com.ai.template.agent.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Getter
public class AgentConfig {

    
    @Value("${article.agent.orchestrator.enabled:true}")
    private boolean orchestratorEnabled;

    
    @Value("${article.agent.max-iterations:10}")
    private int maxIterations;

    
    @Bean
    public MemorySaver memorySaver() {
        return new MemorySaver();
    }
}
