package com.example.agent.model.dto;

import com.example.agent.enums.ToolType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具调用DTO
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallDto {

    /**
     * 工具ID
     */
    private String toolId;

    /**
     * 工具类型
     */
    private ToolType toolType;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具参数
     */
    private Map<String, Object> parameters;

    /**
     * 执行结果
     */
    private Object result;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

}
