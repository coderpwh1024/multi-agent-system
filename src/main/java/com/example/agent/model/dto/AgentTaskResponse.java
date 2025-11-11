package com.example.agent.model.dto;

import com.example.agent.enums.AgentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent任务响应DTO
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTaskResponse {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务状态
     */
    private AgentStatus status;

    /**
     * 执行步骤列表
     */
    private List<AgentStepDto> steps;

    /**
     * 最终结果
     */
    private String result;

    /**
     * 总迭代次数
     */
    private Integer totalIterations;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 错误信息
     */
    private String errorMessage;

}
