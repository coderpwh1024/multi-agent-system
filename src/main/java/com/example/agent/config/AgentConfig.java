package com.example.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Agent配置
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "agent")
public class AgentConfig {

    /**
     * 最大迭代次数
     */
    private Integer maxIterations = 10;

    /**
     * 超时时间（秒）
     */
    private Integer timeout = 300;

    /**
     * 最大并发agent数量
     */
    private Integer maxConcurrentAgents = 5;

}
