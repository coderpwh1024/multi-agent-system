<<<<<<< HEAD
# Multi-Agent System

基于 Spring Boot 3.4 + Azure OpenAI 的多智能体系统，采用 Anthropic 的 Building Effective Agents 模式。

## 技术栈

- **后端框架**: Spring Boot 3.4
- **AI服务**: Azure OpenAI (流式输出)
- **数据库**: MySQL 8.0+
- **缓存**: Redis 7.0+
- **搜索引擎**: Elasticsearch 8.0+
- **ORM**: MyBatis Plus 3.5.5
- **连接池**: Druid
- **响应式编程**: Spring WebFlux, Reactor

## 核心特性

### 1. Anthropic Agent 模式

采用 Anthropic 推荐的 Agent 工作流模式：

- **思考 (Thinking)**: Agent 分析问题，制定策略
- **行动 (Action)**: 执行具体操作或调用工具
- **观察 (Observation)**: 观察行动结果
- **反思 (Reflection)**: 根据结果调整策略

### 2. 多角色 Agent

- **协调者 (Coordinator)**: 负责任务分解、规划和协调
- **研究员 (Researcher)**: 负责信息检索、数据分析
- **执行者 (Executor)**: 负责具体任务执行
- **审核者 (Reviewer)**: 负责结果验证、质量控制
- **工具调用者 (Tool Caller)**: 负责调用各种工具和 API

### 3. 可扩展工具系统

- **搜索工具**: 使用 Elasticsearch 进行全文检索
- **缓存工具**: 使用 Redis 进行数据缓存
- **数据库查询工具**: 查询 MySQL 数据库
- **HTTP 请求工具**: 发送 HTTP 请求
- **计算工具**: 进行数学计算
- **文本分析工具**: 文本处理和分析

### 4. 流式输出

使用 Server-Sent Events (SSE) 实现实时流式输出，展示 Agent 的思考和执行过程。

## 项目结构

```
multi-agent-system/
├── src/main/java/com/example/agent/
│   ├── config/                    # 配置类
│   │   ├── AgentConfig.java       # Agent 配置
│   │   ├── AzureOpenAIConfig.java # Azure OpenAI 配置
│   │   └── RedisConfig.java       # Redis 配置
│   ├── controller/                # 控制器
│   │   └── AgentController.java   # Agent API 控制器
│   ├── entity/                    # 数据库实体
│   │   └── AgentTaskRecord.java   # 任务记录实体
│   ├── enums/                     # 枚举类
│   │   ├── AgentRole.java         # Agent 角色枚举
│   │   ├── AgentStatus.java       # Agent 状态枚举
│   │   └── ToolType.java          # 工具类型枚举
│   ├── mapper/                    # MyBatis Mapper
│   │   └── AgentTaskRecordMapper.java
│   ├── model/dto/                 # 数据传输对象
│   │   ├── AgentStepDto.java      # Agent 步骤 DTO
│   │   ├── AgentTaskRequest.java  # 任务请求 DTO
│   │   ├── AgentTaskResponse.java # 任务响应 DTO
│   │   └── ToolCallDto.java       # 工具调用 DTO
│   ├── service/                   # 服务层
│   │   ├── AgentExecutor.java     # Agent 执行引擎
│   │   ├── AgentTaskRecordService.java
│   │   └── AzureOpenAIService.java
│   └── tool/                      # 工具实现
│       ├── AgentTool.java         # 工具接口
│       └── impl/
│           ├── CacheTool.java     # 缓存工具
│           └── SearchTool.java    # 搜索工具
├── src/main/resources/
│   ├── application.yml            # 应用配置
│   └── db/schema.sql             # 数据库脚本
└── pom.xml                       # Maven 配置
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- Elasticsearch 8.0+

### 2. 配置

编辑 `src/main/resources/application.yml`：

```yaml
# Azure OpenAI 配置
azure:
  openai:
    endpoint: ${AZURE_OPENAI_ENDPOINT}
    api-key: ${AZURE_OPENAI_API_KEY}
    deployment-name: ${AZURE_OPENAI_DEPLOYMENT_NAME}

# MySQL 配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/multi_agent
    username: root
    password: your-password

# Redis 配置
  data:
    redis:
      host: localhost
      port: 6379

# Elasticsearch 配置
    elasticsearch:
      uris: http://localhost:9200
```

### 3. 初始化数据库

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

### 4. 启动应用

```bash
mvn spring-boot:run
```

### 5. 测试 API

#### 执行 Agent 任务（流式）

```bash
curl -X POST http://localhost:8080/api/agent/execute/stream \
  -H "Content-Type: application/json" \
  -d '{
    "task": "查询并分析最近的技术文档",
    "role": "RESEARCHER",
    "availableTools": ["search", "cache"],
    "maxIterations": 10,
    "stream": true
  }'
```

#### 查询任务状态

```bash
curl http://localhost:8080/api/agent/task/{taskId}
```

## API 文档

### POST /api/agent/execute/stream

执行 Agent 任务（流式输出）

**请求体**:
```json
{
  "task": "任务描述",
  "role": "COORDINATOR|RESEARCHER|EXECUTOR|REVIEWER|TOOL_CALLER",
  "context": {
    "key": "value"
  },
  "availableTools": ["search", "cache", "database_query"],
  "maxIterations": 10,
  "stream": true
}
```

**响应**: Server-Sent Events (SSE) 流

```
event: step
data: {
  "stepNumber": 1,
  "status": "THINKING",
  "thinking": "分析任务...",
  "action": "调用搜索工具",
  "toolCall": {...},
  "observation": "搜索结果...",
  "startTime": "2025-10-22T10:00:00",
  "endTime": "2025-10-22T10:00:05"
}
```

### GET /api/agent/task/{taskId}

查询任务状态

**响应**:
```json
{
  "taskId": "uuid",
  "status": "COMPLETED",
  "steps": [...],
  "result": "最终结果",
  "totalIterations": 5,
  "startTime": "2025-10-22T10:00:00",
  "endTime": "2025-10-22T10:00:30"
}
```

## 核心概念

### Agent 执行流程

1. **接收任务**: 用户提交任务请求
2. **初始化**: 创建 Agent 实例，加载可用工具
3. **执行循环**:
   - 思考：分析当前状态，制定计划
   - 行动：执行操作或调用工具
   - 观察：获取行动结果
   - 反思：评估结果，决定下一步
4. **完成任务**: 达到目标或最大迭代次数
5. **返回结果**: 流式输出所有步骤和最终结果

### 工具系统

工具是 Agent 与外部系统交互的接口。每个工具实现 `AgentTool` 接口：

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

### 添加自定义工具

1. 实现 `AgentTool` 接口
2. 添加 `@Component` 注解
3. Spring 会自动注册该工具

示例：

```java
@Component
public class CustomTool implements AgentTool {
    @Override
    public ToolType getToolType() {
        return ToolType.CUSTOM;
    }

    @Override
    public Object execute(Map<String, Object> parameters) {
        // 实现工具逻辑
        return result;
    }
}
```

## 代码规范

本项目遵循阿里巴巴 Java 开发手册规范：

- 类名使用 UpperCamelCase
- 方法名、参数名、成员变量使用 lowerCamelCase
- 常量名全部大写，单词间用下划线隔开
- 使用 Lombok 简化代码
- 所有类、方法添加 Javadoc 注释
- 使用 `@author` 和 `@date` 标注作者和日期

## 性能优化

- 使用 Redis 缓存热点数据
- Elasticsearch 提供高性能全文检索
- WebFlux 响应式编程提升并发性能
- Druid 连接池优化数据库访问
- 流式输出减少内存占用

## 监控和调试

- Druid 监控: http://localhost:8080/api/druid/
- 健康检查: http://localhost:8080/api/agent/health
- 日志级别可在 application.yml 中配置

## 常见问题

### Q: 如何更换 AI 模型？

A: 在 application.yml 中修改 `azure.openai.deployment-name`

### Q: 如何增加最大迭代次数？

A: 在 application.yml 中修改 `agent.max-iterations`，或在请求中指定 `maxIterations`

### Q: 如何添加新的 Agent 角色？

A: 在 `AgentRole` 枚举中添加新角色，并在 `AgentExecutor` 中实现对应逻辑

## 许可证

MIT License

## 作者

coderpwh - 2025-10-22
=======
# multi-agent-system
>>>>>>> 7faddce17efc1708d9ade905bcbac38733f580e1
