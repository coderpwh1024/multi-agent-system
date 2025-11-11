package com.example.agent.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.*;
import com.example.agent.config.AzureOpenAIConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * Azure OpenAI服务
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AzureOpenAIService {

    private final OpenAIAsyncClient openAIAsyncClient;
    private final AzureOpenAIConfig azureOpenAIConfig;

    /**
     * 发送聊天消息（流式）
     *
     * @param messages 消息列表
     * @return 流式响应
     */
    public Flux<String> chatCompletionStream(List<ChatRequestMessage> messages) {
        ChatCompletionsOptions options = new ChatCompletionsOptions(messages)
                .setMaxTokens(azureOpenAIConfig.getMaxTokens())
                .setTemperature(azureOpenAIConfig.getTemperature())
                .setStream(true);

        return openAIAsyncClient.getChatCompletionsStream(
                        azureOpenAIConfig.getDeploymentName(),
                        options)
                .flatMap(chatCompletions -> {
                    List<String> contents = new ArrayList<>();
                    for (ChatChoice choice : chatCompletions.getChoices()) {
                        ChatResponseMessage delta = choice.getDelta();
                        if (delta != null && delta.getContent() != null) {
                            contents.add(delta.getContent());
                        }
                    }
                    return Flux.fromIterable(contents);
                })
                .doOnError(error -> log.error("Error in chat completion stream", error));
    }

    /**
     * 发送聊天消息（非流式）
     *
     * @param messages 消息列表
     * @return 响应内容
     */
    public String chatCompletion(List<ChatRequestMessage> messages) {
        ChatCompletionsOptions options = new ChatCompletionsOptions(messages)
                .setMaxTokens(azureOpenAIConfig.getMaxTokens())
                .setTemperature(azureOpenAIConfig.getTemperature());

        try {
            ChatCompletions chatCompletions = openAIAsyncClient.getChatCompletions(
                            azureOpenAIConfig.getDeploymentName(),
                            options)
                    .block();

            if (chatCompletions != null && !chatCompletions.getChoices().isEmpty()) {
                return chatCompletions.getChoices().get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            log.error("Error in chat completion", e);
        }

        return null;
    }

    /**
     * 发送聊天消息（带工具调用，流式）
     *
     * @param messages 消息列表
     * @param tools    工具列表
     * @return 流式响应
     */
    public Flux<ChatCompletions> chatCompletionWithToolsStream(
            List<ChatRequestMessage> messages,
            List<ChatCompletionsFunctionToolDefinition> tools) {

        ChatCompletionsOptions options = new ChatCompletionsOptions(messages)
                .setMaxTokens(azureOpenAIConfig.getMaxTokens())
                .setTemperature(azureOpenAIConfig.getTemperature())
                .setTools(new ArrayList<>(tools))
                .setStream(true);

        return openAIAsyncClient.getChatCompletionsStream(
                        azureOpenAIConfig.getDeploymentName(),
                        options)
                .doOnError(error -> log.error("Error in chat completion with tools stream", error));
    }

    /**
     * 创建系统消息
     *
     * @param content 内容
     * @return 系统消息
     */
    public ChatRequestMessage createSystemMessage(String content) {
        return new ChatRequestSystemMessage(content);
    }

    /**
     * 创建用户消息
     *
     * @param content 内容
     * @return 用户消息
     */
    public ChatRequestMessage createUserMessage(String content) {
        return new ChatRequestUserMessage(content);
    }

    /**
     * 创建助手消息
     *
     * @param content 内容
     * @return 助手消息
     */
    public ChatRequestMessage createAssistantMessage(String content) {
        return new ChatRequestAssistantMessage(content);
    }

}
