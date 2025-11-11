package com.example.agent.tool;

import com.example.agent.enums.ToolType;
import com.example.agent.model.dto.ToolCallDto;

import java.util.Map;

/**
 * Agent工具接口
 *
 * @author coderpwh
 * @date 2025-10-22
 */
public interface AgentTool {

    /**
     * 获取工具类型
     *
     * @return 工具类型
     */
    ToolType getToolType();

    /**
     * 获取工具名称
     *
     * @return 工具名称
     */
    String getToolName();

    /**
     * 获取工具描述
     *
     * @return 工具描述
     */
    String getToolDescription();

    /**
     * 获取工具参数schema
     *
     * @return 参数schema
     */
    Map<String, Object> getParametersSchema();

    /**
     * 执行工具
     *
     * @param parameters 参数
     * @return 执行结果
     */
    Object execute(Map<String, Object> parameters);

    /**
     * 验证参数
     *
     * @param parameters 参数
     * @return 是否有效
     */
    default boolean validateParameters(Map<String, Object> parameters) {
        return true;
    }

}
