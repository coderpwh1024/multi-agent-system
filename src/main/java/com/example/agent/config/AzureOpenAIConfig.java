package com.example.agent.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Azure OpenAI配置
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "azure.openai")
public class AzureOpenAIConfig {

    /**
     * Azure OpenAI端点
     */
    private String endpoint;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 部署名称
     */
    private String deploymentName;

    /**
     * API版本
     */
    private String apiVersion;

    /**
     * 最大token数
     */
    private Integer maxTokens = 4096;

    /**
     * 温度参数
     */
    private Double temperature = 0.7;

    /**
     * 是否启用流式输出
     */
    private Boolean stream = true;

    /**
     * 创建Azure OpenAI异步客户端
     *
     * @return OpenAIAsyncClient
     */
    @Bean
    public OpenAIAsyncClient openAIAsyncClient() {
        return new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(apiKey))
                .buildAsyncClient();
    }

}
