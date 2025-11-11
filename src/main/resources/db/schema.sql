-- 创建数据库
CREATE DATABASE IF NOT EXISTS `multi_agent` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `multi_agent`;

-- Agent任务执行记录表
DROP TABLE IF EXISTS `agent_task_record`;
CREATE TABLE `agent_task_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` varchar(64) NOT NULL COMMENT '任务ID',
  `task_description` text COMMENT '任务描述',
  `agent_role` varchar(32) COMMENT 'Agent角色',
  `status` varchar(32) COMMENT '任务状态',
  `steps` json COMMENT '执行步骤（JSON格式）',
  `result` text COMMENT '最终结果',
  `total_iterations` int DEFAULT 0 COMMENT '总迭代次数',
  `start_time` datetime COMMENT '开始时间',
  `end_time` datetime COMMENT '结束时间',
  `error_message` text COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent任务执行记录表';

-- 知识库文档表（用于Elasticsearch）
DROP TABLE IF EXISTS `knowledge_document`;
CREATE TABLE `knowledge_document` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `doc_id` varchar(64) NOT NULL COMMENT '文档ID',
  `title` varchar(255) NOT NULL COMMENT '文档标题',
  `content` text NOT NULL COMMENT '文档内容',
  `category` varchar(64) COMMENT '文档分类',
  `tags` varchar(255) COMMENT '标签（逗号分隔）',
  `author` varchar(64) COMMENT '作者',
  `source` varchar(255) COMMENT '来源',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_id` (`doc_id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';

-- 插入示例数据
INSERT INTO `knowledge_document` (`doc_id`, `title`, `content`, `category`, `tags`, `author`, `source`)
VALUES
('doc-001', 'Spring Boot入门指南', 'Spring Boot是一个基于Spring框架的快速开发框架...', '技术文档', 'Spring,Java', 'Admin', 'Internal'),
('doc-002', 'Azure OpenAI使用教程', 'Azure OpenAI提供了强大的AI能力...', '技术文档', 'Azure,AI,OpenAI', 'Admin', 'Internal'),
('doc-003', '多智能体系统架构设计', '多智能体系统是一种分布式AI架构...', '架构设计', 'AI,Agent,Architecture', 'Admin', 'Internal');
