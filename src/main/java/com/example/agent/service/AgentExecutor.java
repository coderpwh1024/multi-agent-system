package com.example.agent.service;

import com.alibaba.fastjson2.JSON;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolDefinition;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.FunctionDefinition;
import com.example.agent.config.AgentConfig;
import com.example.agent.enums.AgentRole;
import com.example.agent.enums.AgentStatus;
import com.example.agent.model.dto.*;
import com.example.agent.tool.AgentTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Agent执行引擎
 * 实现Anthropic的Agent模式：思考 -> 行动 -> 观察 -> 反思的循环
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentExecutor {

    private final AzureOpenAIService azureOpenAIService;
    private final AgentConfig agentConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final List<AgentTool> agentTools;

    /**
     * 存储正在执行的任务
     */
    private final Map<String, AgentTaskResponse> runningTasks = new ConcurrentHashMap<>();

    /**
     * 执行Agent任务（流式）
     *
     * @param request 任务请求
     * @return 流式响应
     */
    public Flux<AgentStepDto> executeAgentTask(AgentTaskRequest request) {
        String taskId = request.getTaskId() != null ? request.getTaskId() : UUID.randomUUID().toString();
        request.setTaskId(taskId);

        // 初始化任务响应
        AgentTaskResponse response = AgentTaskResponse.builder()
                .taskId(taskId)
                .status(AgentStatus.INITIALIZED)
                .steps(new ArrayList<>())
                .startTime(LocalDateTime.now())
                .totalIterations(0)
                .build();

        runningTasks.put(taskId, response);

        // 保存到Redis
        saveTaskToRedis(taskId, response);

        // 创建Sink用于发送步骤更新
        Sinks.Many<AgentStepDto> sink = Sinks.many().multicast().onBackpressureBuffer();

        // 异步执行Agent循环
        executeAgentLoop(request, response, sink);

        return sink.asFlux();
    }

    /**
     * 执行Agent循环
     *
     * @param request  请求
     * @param response 响应
     * @param sink     步骤发送器
     */
    private void executeAgentLoop(AgentTaskRequest request, AgentTaskResponse response, Sinks.Many<AgentStepDto> sink) {
        // 获取最大迭代次数
        int maxIterations = request.getMaxIterations() != null ?
                request.getMaxIterations() : agentConfig.getMaxIterations();

        // 构建系统提示词
        String systemPrompt = buildSystemPrompt(request.getRole(), request.getAvailableTools());

        // 初始化消息列表
        List<ChatRequestMessage> messages = new ArrayList<>();
        messages.add(azureOpenAIService.createSystemMessage(systemPrompt));
        messages.add(azureOpenAIService.createUserMessage(request.getTask()));

        // 执行循环
        for (int i = 0; i < maxIterations; i++) {
            AgentStepDto step = executeStep(i + 1, messages, request);

            // 发送步骤更新
            sink.tryEmitNext(step);

            // 添加到响应
            response.getSteps().add(step);
            response.setTotalIterations(i + 1);

            // 更新Redis
            saveTaskToRedis(request.getTaskId(), response);

            // 检查是否完成
            if (step.getStatus() == AgentStatus.COMPLETED || step.getStatus() == AgentStatus.FAILED) {
                response.setStatus(step.getStatus());
                response.setEndTime(LocalDateTime.now());
                response.setResult(step.getObservation());
                saveTaskToRedis(request.getTaskId(), response);
                sink.tryEmitComplete();
                return;
            }

            // 添加助手消息到对话历史
            if (step.getAction() != null) {
                messages.add(azureOpenAIService.createAssistantMessage(step.getAction()));
            }

            // 添加观察结果到对话历史
            if (step.getObservation() != null) {
                messages.add(azureOpenAIService.createUserMessage("观察结果: " + step.getObservation()));
            }
        }

        // 达到最大迭代次数
        response.setStatus(AgentStatus.COMPLETED);
        response.setEndTime(LocalDateTime.now());
        response.setResult("已达到最大迭代次数");
        saveTaskToRedis(request.getTaskId(), response);
        sink.tryEmitComplete();
    }

    /**
     * 执行单个步骤
     *
     * @param stepNumber 步骤编号
     * @param messages   消息列表
     * @param request    请求
     * @return 步骤结果
     */
    private AgentStepDto executeStep(int stepNumber, List<ChatRequestMessage> messages, AgentTaskRequest request) {
        AgentStepDto step = AgentStepDto.builder()
                .stepNumber(stepNumber)
                .status(AgentStatus.THINKING)
                .startTime(LocalDateTime.now())
                .build();

        try {
            log.info("Executing step {}", stepNumber);

            // 调用OpenAI获取响应
            StringBuilder responseBuilder = new StringBuilder();
            azureOpenAIService.chatCompletionStream(messages)
                    .doOnNext(responseBuilder::append)
                    .blockLast();

            String response = responseBuilder.toString();
            log.debug("AI Response: {}", response);

            // 解析响应
            step.setThinking(extractThinking(response));
            step.setAction(extractAction(response));

            // 检查是否需要工具调用
            ToolCallDto toolCall = extractToolCall(response, request.getAvailableTools());

            if (toolCall != null) {
                step.setStatus(AgentStatus.EXECUTING);
                step.setToolCall(toolCall);

                // 执行工具
                Object toolResult = executeToolCall(toolCall);
                toolCall.setResult(toolResult);
                toolCall.setSuccess(true);

                step.setObservation(JSON.toJSONString(toolResult));
            } else {
                // 检查是否完成
                if (response.contains("FINAL_ANSWER:") || response.contains("任务完成")) {
                    step.setStatus(AgentStatus.COMPLETED);
                    step.setObservation(extractFinalAnswer(response));
                } else {
                    step.setStatus(AgentStatus.WAITING);
                    step.setObservation(response);
                }
            }

        } catch (Exception e) {
            log.error("Error executing step {}", stepNumber, e);
            step.setStatus(AgentStatus.FAILED);
            step.setObservation("执行失败: " + e.getMessage());
        }

        step.setEndTime(LocalDateTime.now());
        return step;
    }

    /**
     * 执行工具调用
     *
     * @param toolCall 工具调用信息
     * @return 执行结果
     */
    private Object executeToolCall(ToolCallDto toolCall) {
        try {
            // 查找对应的工具
            Optional<AgentTool> toolOpt = agentTools.stream()
                    .filter(tool -> tool.getToolName().equals(toolCall.getToolName()))
                    .findFirst();

            if (toolOpt.isPresent()) {
                AgentTool tool = toolOpt.get();

                // 验证参数
                if (!tool.validateParameters(toolCall.getParameters())) {
                    throw new IllegalArgumentException("Invalid tool parameters");
                }

                // 执行工具
                return tool.execute(toolCall.getParameters());
            } else {
                throw new IllegalArgumentException("Tool not found: " + toolCall.getToolName());
            }

        } catch (Exception e) {
            log.error("Error executing tool: {}", toolCall.getToolName(), e);
            toolCall.setSuccess(false);
            toolCall.setErrorMessage(e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    /**
     * 构建系统提示词
     *
     * @param role           Agent角色
     * @param availableTools 可用工具列表
     * @return 系统提示词
     */
    private String buildSystemPrompt(AgentRole role, List<String> availableTools) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个").append(role.getName()).append("。\n");
        prompt.append(role.getDescription()).append("\n\n");

        prompt.append("你需要按照以下步骤思考和行动:\n");
        prompt.append("1. 思考(Thinking): 分析当前任务,制定解决方案\n");
        prompt.append("2. 行动(Action): 执行具体操作或调用工具\n");
        prompt.append("3. 观察(Observation): 观察行动结果\n");
        prompt.append("4. 反思(Reflection): 根据结果调整策略\n\n");

        if (availableTools != null && !availableTools.isEmpty()) {
            prompt.append("可用工具:\n");
            availableTools.forEach(toolName -> {
                agentTools.stream()
                        .filter(tool -> tool.getToolName().equals(toolName))
                        .findFirst()
                        .ifPresent(tool -> prompt.append("- ").append(tool.getToolName())
                                .append(": ").append(tool.getToolDescription()).append("\n"));
            });
            prompt.append("\n");
        }

        prompt.append("请使用以下格式回复:\n");
        prompt.append("思考: [你的思考过程]\n");
        prompt.append("行动: [你的行动或工具调用]\n");
        prompt.append("如果任务完成,请使用: FINAL_ANSWER: [最终答案]\n");

        return prompt.toString();
    }

    /**
     * 提取思考内容
     */
    private String extractThinking(String response) {
        return extractSection(response, "思考:", "行动:");
    }

    /**
     * 提取行动内容
     */
    private String extractAction(String response) {
        return extractSection(response, "行动:", "FINAL_ANSWER:");
    }

    /**
     * 提取最终答案
     */
    private String extractFinalAnswer(String response) {
        int index = response.indexOf("FINAL_ANSWER:");
        if (index >= 0) {
            return response.substring(index + "FINAL_ANSWER:".length()).trim();
        }
        return response;
    }

    /**
     * 提取文本段落
     */
    private String extractSection(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        if (startIndex < 0) {
            return null;
        }

        startIndex += start.length();
        int endIndex = text.indexOf(end, startIndex);

        if (endIndex < 0) {
            return text.substring(startIndex).trim();
        }

        return text.substring(startIndex, endIndex).trim();
    }

    /**
     * 提取工具调用
     */
    private ToolCallDto extractToolCall(String response, List<String> availableTools) {
        // 简单的工具调用解析逻辑
        // 实际实现中应该使用更复杂的解析方式或使用OpenAI的function calling功能
        if (availableTools == null || availableTools.isEmpty()) {
            return null;
        }

        // TODO: 实现更复杂的工具调用解析逻辑
        return null;
    }

    /**
     * 保存任务到Redis
     */
    private void saveTaskToRedis(String taskId, AgentTaskResponse response) {
        try {
            String key = "agent:task:" + taskId;
            redisTemplate.opsForValue().set(key, response);
        } catch (Exception e) {
            log.error("Error saving task to Redis", e);
        }
    }

    /**
     * 从Redis获取任务
     */
    public AgentTaskResponse getTaskFromRedis(String taskId) {
        try {
            String key = "agent:task:" + taskId;
            return (AgentTaskResponse) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting task from Redis", e);
            return null;
        }
    }

}
