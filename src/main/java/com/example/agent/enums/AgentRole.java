package com.example.agent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Agent角色枚举
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Getter
@AllArgsConstructor
public enum AgentRole {

    /**
     * 协调者 - 负责任务分解和协调
     */
    COORDINATOR("coordinator", "协调者", "负责任务分解、规划和协调其他Agent"),

    /**
     * 研究员 - 负责信息检索和分析
     */
    RESEARCHER("researcher", "研究员", "负责信息检索、数据分析和知识整理"),

    /**
     * 执行者 - 负责具体任务执行
     */
    EXECUTOR("executor", "执行者", "负责具体任务的执行和操作"),

    /**
     * 审核者 - 负责结果验证和质量控制
     */
    REVIEWER("reviewer", "审核者", "负责结果验证、质量检查和反馈"),

    /**
     * 工具调用者 - 负责工具调用
     */
    TOOL_CALLER("tool_caller", "工具调用者", "负责调用各种工具和API");

    /**
     * 角色代码
     */
    private final String code;

    /**
     * 角色名称
     */
    private final String name;

    /**
     * 角色描述
     */
    private final String description;

}
