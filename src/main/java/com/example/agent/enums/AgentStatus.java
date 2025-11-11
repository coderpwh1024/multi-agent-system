package com.example.agent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Agent状态枚举
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Getter
@AllArgsConstructor
public enum AgentStatus {

    /**
     * 初始化
     */
    INITIALIZED("initialized", "初始化"),

    /**
     * 思考中
     */
    THINKING("thinking", "思考中"),

    /**
     * 执行中
     */
    EXECUTING("executing", "执行中"),

    /**
     * 等待中
     */
    WAITING("waiting", "等待中"),

    /**
     * 已完成
     */
    COMPLETED("completed", "已完成"),

    /**
     * 失败
     */
    FAILED("failed", "失败");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

}
