# 多智能体系统架构设计文档

## 1. 系统概述

本系统是一个基于 Spring Boot 3.4 和 Azure OpenAI 的多智能体系统，采用 Anthropic 推荐的 Building Effective Agents 模式，实现了智能任务规划、执行和协调功能。

## 2. 核心设计理念

### 2.1 Anthropic Agent 模式

系统采用 Anthropic 提出的 Agentic Loop 模式，包含以下核心环节：

```
思考 (Thinking) → 行动 (Action) → 观察 (Observation) → 反思 (Reflection)
     ↑                                                           ↓
     └───────────────────────────────────────────────────────────┘
```

**核心流程**：

1. **思考 (Thinking)**: Agent 分析当前任务和上下文，制定解决策略
2. **行动 (Action)**: 根据策略执行具体操作或调用工具
3. **观察 (Observation)**: 获取行动的执行结果和反馈
4. **反思 (Reflection)**: 评估结果，调整策略，决定下一步行动

### 2.2 多角色协作

系统支持多种 Agent 角色，每个角色有专门的职责：

- **协调者 (Coordinator)**: 任务分解、规划和多 Agent 协调
- **研究员 (Researcher)**: 信息检索、数据分析和知识整理
- **执行者 (Executor)**: 具体任务执行和操作
- **审核者 (Reviewer)**: 结果验证和质量控制
- **工具调用者 (Tool Caller)**: 专门负责工具和 API 调用

## 3. 技术架构

### 3.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│                    (HTTP/SSE Clients)                        │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                   Controller Layer                           │
│                  (AgentController)                           │
│                  - REST API                                  │
│                  - SSE Streaming                             │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                    Service Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │AgentExecutor │  │AzureOpenAI   │  │TaskRecord    │      │
│  │(核心引擎)    │  │Service       │  │Service       │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                     Tool Layer                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │Search    │  │Cache     │  │Database  │  │HTTP      │   │
│  │Tool      │  │Tool      │  │Tool      │  │Tool      │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                 Infrastructure Layer                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │Azure     │  │MySQL     │  │Redis     │  │Elastic   │   │
│  │OpenAI    │  │          │  │          │  │search    │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 核心组件

#### 3.2.1 AgentExecutor (执行引擎)

**职责**：
- 管理 Agent 执行生命周期
- 实现 Agentic Loop 循环
- 协调工具调用
- 状态管理和持久化

**关键方法**：
```java
public Flux<AgentStepDto> executeAgentTask(AgentTaskRequest request)
private void executeAgentLoop(...)
private AgentStepDto executeStep(...)
private Object executeToolCall(ToolCallDto toolCall)
```

#### 3.2.2 AzureOpenAIService

**职责**：
- 封装 Azure OpenAI API 调用
- 支持流式和非流式输出
- 管理消息历史
- 支持 Function Calling

**关键方法**：
```java
public Flux<String> chatCompletionStream(List<ChatRequestMessage> messages)
public String chatCompletion(List<ChatRequestMessage> messages)
public Flux<ChatCompletions> chatCompletionWithToolsStream(...)
```

#### 3.2.3 Tool System

**设计模式**: 策略模式 + 工厂模式

**接口定义**：
```java
public interface AgentTool {
    ToolType getToolType();
    String getToolName();
    String getToolDescription();
    Map<String, Object> getParametersSchema();
    Object execute(Map<String, Object> parameters);
    boolean validateParameters(Map<String, Object> parameters);
}
```

**工具注册**: Spring 自动扫描和注入所有实现 `AgentTool` 接口的 `@Component`

## 4. 数据流设计

### 4.1 请求处理流程

```
1. Client 发送任务请求
   ↓
2. Controller 接收并验证请求
   ↓
3. AgentExecutor 创建任务实例
   ↓
4. 初始化 Agent 上下文和消息历史
   ↓
5. 开始 Agentic Loop:
   ├─ 调用 Azure OpenAI 获取思考和行动
   ├─ 解析响应，提取工具调用
   ├─ 执行工具，获取观察结果
   ├─ 将结果反馈给 AI 进行反思
   └─ 判断是否完成或继续循环
   ↓
6. 返回最终结果
   ↓
7. 保存任务记录到 MySQL
```

### 4.2 流式输出设计

使用 **Server-Sent Events (SSE)** 实现实时流式输出：

```java
// 创建 Flux 流
Flux<ServerSentEvent<AgentStepDto>> stream =
    agentExecutor.executeAgentTask(request)
        .map(step -> ServerSentEvent.<AgentStepDto>builder()
            .id(String.valueOf(step.getStepNumber()))
            .event("step")
            .data(step)
            .build());
```

**优势**：
- 实时展示 Agent 思考过程
- 提升用户体验
- 支持长时间任务
- 降低内存占用

## 5. 数据模型设计

### 5.1 核心实体

#### AgentTaskRequest
```java
{
  "taskId": "uuid",
  "task": "任务描述",
  "role": "COORDINATOR",
  "context": {...},
  "availableTools": ["search", "cache"],
  "maxIterations": 10,
  "stream": true
}
```

#### AgentStepDto
```java
{
  "stepNumber": 1,
  "status": "EXECUTING",
  "thinking": "分析任务...",
  "action": "调用搜索工具",
  "toolCall": {...},
  "observation": "搜索结果...",
  "startTime": "2025-10-22T10:00:00",
  "endTime": "2025-10-22T10:00:05"
}
```

#### AgentTaskResponse
```java
{
  "taskId": "uuid",
  "status": "COMPLETED",
  "steps": [...],
  "result": "最终结果",
  "totalIterations": 5,
  "startTime": "...",
  "endTime": "..."
}
```

### 5.2 数据库设计

**agent_task_record 表**：
- id: 主键
- task_id: 任务 ID (唯一索引)
- task_description: 任务描述
- agent_role: Agent 角色
- status: 任务状态
- steps: 执行步骤 (JSON)
- result: 最终结果
- total_iterations: 总迭代次数
- start_time/end_time: 时间戳
- create_time/update_time: 审计字段

## 6. 缓存策略

### 6.1 Redis 缓存使用

**缓存场景**：
1. **任务状态缓存**: `agent:task:{taskId}` → AgentTaskResponse
2. **工具结果缓存**: `tool:result:{hash}` → 工具执行结果
3. **会话上下文**: `agent:context:{sessionId}` → 上下文信息

**过期策略**：
- 任务状态: 1 小时
- 工具结果: 根据工具类型配置 (默认 30 分钟)
- 会话上下文: 2 小时

## 7. 性能优化

### 7.1 并发控制

使用 `Semaphore` 限制并发 Agent 数量：
```java
private final Semaphore semaphore = new Semaphore(maxConcurrentAgents);
```

### 7.2 异步处理

使用 Spring `@Async` 和 Reactor 实现异步执行：
```java
@Async
public CompletableFuture<AgentTaskResponse> executeAsync(...)
```

### 7.3 连接池优化

- **Druid**: 数据库连接池
  - initial-size: 5
  - max-active: 20

- **Lettuce**: Redis 连接池
  - max-active: 8
  - max-idle: 8

## 8. 安全设计

### 8.1 API 安全

- 输入验证: `@Valid` + 自定义验证器
- SQL 注入防护: MyBatis Plus 参数化查询
- XSS 防护: 输出转义

### 8.2 数据安全

- 敏感信息加密存储
- API Key 环境变量管理
- 逻辑删除机制

## 9. 监控和日志

### 9.1 日志规范

遵循阿里巴巴日志规范：
- ERROR: 系统错误，需要立即处理
- WARN: 警告信息，可能影响系统
- INFO: 重要业务流程
- DEBUG: 详细调试信息

### 9.2 监控指标

- Agent 执行时间
- 工具调用成功率
- API 调用延迟
- 系统资源使用

## 10. 扩展性设计

### 10.1 工具扩展

实现 `AgentTool` 接口即可添加新工具：
```java
@Component
public class CustomTool implements AgentTool {
    // 实现接口方法
}
```

### 10.2 角色扩展

在 `AgentRole` 枚举中添加新角色，并在 `AgentExecutor` 中实现对应逻辑。

### 10.3 多模型支持

通过配置切换不同的 AI 模型：
```yaml
azure:
  openai:
    deployment-name: gpt-4 # 或 gpt-4-turbo, gpt-3.5-turbo
```

## 11. 最佳实践

### 11.1 代码规范

遵循阿里巴巴 Java 开发手册：
- 命名规范
- 注释规范
- 异常处理
- 并发控制

### 11.2 Agent 设计建议

1. **明确角色职责**: 每个 Agent 角色应有清晰的职责边界
2. **合理设置迭代次数**: 根据任务复杂度设置 `maxIterations`
3. **工具选择**: 为 Agent 提供合适的工具集
4. **提示词优化**: 为不同角色设计专门的系统提示词
5. **错误处理**: 实现完善的异常处理和重试机制

### 11.3 性能优化建议

1. 使用缓存减少重复计算
2. 异步执行长时间任务
3. 合理设置超时时间
4. 限制并发 Agent 数量
5. 定期清理历史数据

## 12. 未来规划

- [ ] 支持多 Agent 并行协作
- [ ] 实现 Agent 之间的消息传递
- [ ] 添加更多工具 (邮件、文件处理等)
- [ ] 支持自定义提示词模板
- [ ] 添加 Agent 性能分析和可视化
- [ ] 支持更多 AI 模型 (Claude, Gemini 等)
- [ ] 实现 Agent 学习和优化机制

## 13. 参考资料

- [Anthropic - Building effective agents](https://docs.claude.com/en/docs/build-with-claude/agent-patterns)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Azure OpenAI Service](https://azure.microsoft.com/en-us/products/ai-services/openai-service)
- [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)
