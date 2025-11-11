package com.example.agent.tool.impl;

import com.example.agent.enums.ToolType;
import com.example.agent.tool.AgentTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存工具实现
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheTool implements AgentTool {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public ToolType getToolType() {
        return ToolType.CACHE;
    }

    @Override
    public String getToolName() {
        return "cache";
    }

    @Override
    public String getToolDescription() {
        return "缓存工具，使用Redis进行数据缓存。参数：operation(操作类型：get/set/delete), key(缓存键), value(缓存值，set操作时需要), ttl(过期时间，秒)";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> operationProperty = new HashMap<>();
        operationProperty.put("type", "string");
        operationProperty.put("description", "操作类型：get, set, delete");
        operationProperty.put("enum", List.of("get", "set", "delete"));
        properties.put("operation", operationProperty);

        Map<String, Object> keyProperty = new HashMap<>();
        keyProperty.put("type", "string");
        keyProperty.put("description", "缓存键");
        properties.put("key", keyProperty);

        Map<String, Object> valueProperty = new HashMap<>();
        valueProperty.put("type", "object");
        valueProperty.put("description", "缓存值（set操作时需要）");
        properties.put("value", valueProperty);

        Map<String, Object> ttlProperty = new HashMap<>();
        ttlProperty.put("type", "integer");
        ttlProperty.put("description", "过期时间（秒）");
        ttlProperty.put("default", 3600);
        properties.put("ttl", ttlProperty);

        schema.put("properties", properties);
        schema.put("required", List.of("operation", "key"));

        return schema;
    }

    @Override
    public Object execute(Map<String, Object> parameters) {
        try {
            String operation = (String) parameters.get("operation");
            String key = (String) parameters.get("key");

            log.info("Executing cache operation: operation={}, key={}", operation, key);

            Map<String, Object> response = new HashMap<>();

            switch (operation.toLowerCase()) {
                case "get":
                    Object value = redisTemplate.opsForValue().get(key);
                    response.put("success", true);
                    response.put("value", value);
                    response.put("exists", value != null);
                    break;

                case "set":
                    Object cacheValue = parameters.get("value");
                    Integer ttl = parameters.containsKey("ttl") ? (Integer) parameters.get("ttl") : 3600;

                    if (ttl > 0) {
                        redisTemplate.opsForValue().set(key, cacheValue, ttl, TimeUnit.SECONDS);
                    } else {
                        redisTemplate.opsForValue().set(key, cacheValue);
                    }

                    response.put("success", true);
                    response.put("message", "缓存设置成功");
                    break;

                case "delete":
                    Boolean deleted = redisTemplate.delete(key);
                    response.put("success", true);
                    response.put("deleted", deleted);
                    break;

                default:
                    response.put("success", false);
                    response.put("error", "不支持的操作类型: " + operation);
            }

            return response;

        } catch (Exception e) {
            log.error("Error executing cache tool", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

    @Override
    public boolean validateParameters(Map<String, Object> parameters) {
        if (!parameters.containsKey("operation") || !parameters.containsKey("key")) {
            return false;
        }

        String operation = (String) parameters.get("operation");
        if ("set".equalsIgnoreCase(operation)) {
            return parameters.containsKey("value");
        }

        return true;
    }

}
