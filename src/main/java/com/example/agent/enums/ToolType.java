package com.example.agent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工具类型枚举
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Getter
@AllArgsConstructor
public enum ToolType {

    /**
     * 搜索工具
     */
    SEARCH("search", "搜索工具", "使用Elasticsearch进行全文检索"),

    /**
     * 数据库查询工具
     */
    DATABASE_QUERY("database_query", "数据库查询", "查询MySQL数据库"),

    /**
     * 缓存工具
     */
    CACHE("cache", "缓存工具", "使用Redis进行数据缓存"),

    /**
     * HTTP请求工具
     */
    HTTP_REQUEST("http_request", "HTTP请求", "发送HTTP请求"),

    /**
     * 计算工具
     */
    CALCULATOR("calculator", "计算工具", "进行数学计算"),

    /**
     * 文本分析工具
     */
    TEXT_ANALYSIS("text_analysis", "文本分析", "进行文本分析和处理");

    /**
     * 工具代码
     */
    private final String code;

    /**
     * 工具名称
     */
    private final String name;

    /**
     * 工具描述
     */
    private final String description;

}
