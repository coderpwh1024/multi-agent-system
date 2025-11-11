package com.example.agent.controller;

import com.example.agent.model.dto.AgentStepDto;
import com.example.agent.model.dto.AgentTaskRequest;
import com.example.agent.model.dto.AgentTaskResponse;
import com.example.agent.service.AgentExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Agent控制器
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Slf4j
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentExecutor agentExecutor;

    /**
     * 执行Agent任务（流式）
     *
     * @param request 任务请求
     * @return SSE流式响应
     */
    @PostMapping(value = "/execute/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AgentStepDto>> executeAgentTaskStream(@RequestBody AgentTaskRequest request) {
        log.info("Received agent task request: {}", request);

        return agentExecutor.executeAgentTask(request)
                .map(step -> ServerSentEvent.<AgentStepDto>builder()
                        .id(String.valueOf(step.getStepNumber()))
                        .event("step")
                        .data(step)
                        .build())
                .doOnComplete(() -> log.info("Agent task completed"))
                .doOnError(error -> log.error("Error in agent task execution", error));
    }

    /**
     * 获取任务状态
     *
     * @param taskId 任务ID
     * @return 任务响应
     */
    @GetMapping("/task/{taskId}")
    public AgentTaskResponse getTaskStatus(@PathVariable String taskId) {
        log.info("Getting task status for: {}", taskId);

        AgentTaskResponse response = agentExecutor.getTaskFromRedis(taskId);

        if (response == null) {
            log.warn("Task not found: {}", taskId);
        }

        return response;
    }

    /**
     * 健康检查
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }

}
