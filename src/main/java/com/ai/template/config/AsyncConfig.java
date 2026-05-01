package com.ai.template.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@EnableAsync
public class AsyncConfig {

    
    @Bean(name = "articleExecutor")
    public Executor articleExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(5);
        
        executor.setMaxPoolSize(10);
        
        executor.setQueueCapacity(100);
        
        executor.setThreadNamePrefix("article-async-");
        
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        return executor;
    }
}