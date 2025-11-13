package com.example.agent.model.dto;

import com.example.agent.enums.AgentRole;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Agent任务请求DTO
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTaskRequest {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务描述
     */
    private String task;

    /**
     * Agent角色
     */
    @JsonAlias({"role", "Role"})
    private AgentRole role;

    /**
     * 上下文信息
     */
    private Map<String, Object> context;

    /**
     * 可用工具列表
     */
    private List<String> availableTools;

    /**
     * 最大迭代次数
     */
    private Integer maxIterations;

    /**
     * 是否启用流式输出
     */
    private Boolean stream;

}
