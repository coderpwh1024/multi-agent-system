package com.example.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 多智能体系统启动类
 *
 * @author coderpwh
 * @date 2025-10-22
 */
@EnableAsync
@EnableCaching
@SpringBootApplication
@MapperScan("com.example.agent.mapper")
public class MultiAgentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiAgentSystemApplication.class, args);
    }

}
