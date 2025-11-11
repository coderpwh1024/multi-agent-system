package com.example.agent.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.agent.enums.AgentRole;
import com.example.agent.enums.AgentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent任务执行记录
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("agent_task_record")
public class AgentTaskRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务描述
     */
    private String taskDescription;

    /**
     * Agent角色
     */
    private AgentRole agentRole;

    /**
     * 任务状态
     */
    private AgentStatus status;

    /**
     * 执行步骤（JSON格式）
     */
    private String steps;

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

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer deleted;

}
