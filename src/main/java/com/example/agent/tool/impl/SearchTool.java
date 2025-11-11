package com.example.agent.tool.impl;

import com.example.agent.enums.ToolType;
import com.example.agent.tool.AgentTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索工具实现
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchTool implements AgentTool {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public ToolType getToolType() {
        return ToolType.SEARCH;
    }

    @Override
    public String getToolName() {
        return "search";
    }

    @Override
    public String getToolDescription() {
        return "搜索工具，使用Elasticsearch进行全文检索。参数：query(搜索关键词), index(索引名称), size(返回数量)";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> queryProperty = new HashMap<>();
        queryProperty.put("type", "string");
        queryProperty.put("description", "搜索关键词");
        properties.put("query", queryProperty);

        Map<String, Object> indexProperty = new HashMap<>();
        indexProperty.put("type", "string");
        indexProperty.put("description", "索引名称");
        properties.put("index", indexProperty);

        Map<String, Object> sizeProperty = new HashMap<>();
        sizeProperty.put("type", "integer");
        sizeProperty.put("description", "返回结果数量");
        sizeProperty.put("default", 10);
        properties.put("size", sizeProperty);

        schema.put("properties", properties);
        schema.put("required", List.of("query", "index"));

        return schema;
    }

    @Override
    public Object execute(Map<String, Object> parameters) {
        try {
            String query = (String) parameters.get("query");
            String index = (String) parameters.get("index");
            Integer size = parameters.containsKey("size") ? (Integer) parameters.get("size") : 10;

            log.info("Executing search: query={}, index={}, size={}", query, index, size);

            // 使用Elasticsearch进行搜索
            Criteria criteria = new Criteria("content").contains(query);
            CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);
            criteriaQuery.setMaxResults(size);

            SearchHits<?> searchHits = elasticsearchOperations.search(
                    criteriaQuery,
                    Object.class
            );

            List<Map<String, Object>> results = searchHits.getSearchHits().stream()
                    .map(hit -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("id", hit.getId());
                        result.put("score", hit.getScore());
                        result.put("content", hit.getContent());
                        return result;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", searchHits.getTotalHits());
            response.put("results", results);

            return response;

        } catch (Exception e) {
            log.error("Error executing search tool", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    @Override
    public boolean validateParameters(Map<String, Object> parameters) {
        return parameters.containsKey("query") && parameters.containsKey("index");
    }

}
