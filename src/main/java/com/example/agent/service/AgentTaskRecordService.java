package com.example.agent.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agent.entity.AgentTaskRecord;
import com.example.agent.mapper.AgentTaskRecordMapper;
import com.example.agent.model.dto.AgentTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Agent任务记录服务
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@Slf4j
@Service
public class AgentTaskRecordService extends ServiceImpl<AgentTaskRecordMapper, AgentTaskRecord> {

    /**
     * 保存任务记录
     *
     * @param response 任务响应
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveTaskRecord(AgentTaskResponse response) {
        try {
            AgentTaskRecord record = AgentTaskRecord.builder()
                    .taskId(response.getTaskId())
                    .status(response.getStatus())
                    .steps(JSON.toJSONString(response.getSteps()))
                    .result(response.getResult())
                    .totalIterations(response.getTotalIterations())
                    .startTime(response.getStartTime())
                    .endTime(response.getEndTime())
                    .errorMessage(response.getErrorMessage())
                    .build();

            this.save(record);
            log.info("Task record saved: {}", response.getTaskId());

        } catch (Exception e) {
            log.error("Error saving task record", e);
            throw e;
        }
    }

    /**
     * 根据任务ID查询记录
     *
     * @param taskId 任务ID
     * @return 任务记录
     */
    public AgentTaskRecord getByTaskId(String taskId) {
        LambdaQueryWrapper<AgentTaskRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentTaskRecord::getTaskId, taskId);
        return this.getOne(wrapper);
    }

    /**
     * 查询最近的任务记录
     *
     * @param limit 数量限制
     * @return 任务记录列表
     */
    public List<AgentTaskRecord> getRecentTasks(int limit) {
        LambdaQueryWrapper<AgentTaskRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AgentTaskRecord::getCreateTime)
                .last("LIMIT " + limit);
        return this.list(wrapper);
    }

}
