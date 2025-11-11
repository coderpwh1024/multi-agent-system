package com.example.agent.model.dto;

import com.example.agent.enums.AgentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent执行步骤DTO
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentStepDto {

    /**
     * 步骤序号
     */
    private Integer stepNumber;

    /**
     * 步骤状态
     */
    private AgentStatus status;

    /**
     * 思考内容
     */
    private String thinking;

    /**
     * 行动内容
     */
    private String action;

    /**
     * 工具调用
     */
    private ToolCallDto toolCall;

    /**
     * 观察结果
     */
    private String observation;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

}
