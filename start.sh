#!/bin/bash

# Multi-Agent System 快速启动脚本

echo "==================================="
echo "Multi-Agent System 快速启动"
echo "==================================="
echo ""

# 检查Java版本
echo "1. 检查Java版本..."
java -version 2>&1 | grep -q "version \"17"
if [ $? -ne 0 ]; then
    echo "❌ 错误: 需要 JDK 17 或更高版本"
    exit 1
fi
echo "✅ Java版本检查通过"
echo ""

# 检查Maven
echo "2. 检查Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven，请先安装Maven"
    exit 1
fi
echo "✅ Maven检查通过"
echo ""

# 检查环境变量
echo "3. 检查环境变量..."
if [ -z "$AZURE_OPENAI_ENDPOINT" ] || [ -z "$AZURE_OPENAI_API_KEY" ]; then
    echo "⚠️  警告: 未设置Azure OpenAI环境变量"
    echo "请设置以下环境变量:"
    echo "  - AZURE_OPENAI_ENDPOINT"
    echo "  - AZURE_OPENAI_API_KEY"
    echo "  - AZURE_OPENAI_DEPLOYMENT_NAME"
    echo ""
    echo "或者在 application.yml 中直接配置"
fi
echo ""

# 检查数据库连接
echo "4. 检查MySQL连接..."
if command -v mysql &> /dev/null; then
    mysql -h localhost -u root -p -e "SELECT 1" 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "✅ MySQL连接成功"

        # 询问是否初始化数据库
        read -p "是否初始化数据库? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "初始化数据库..."
            mysql -h localhost -u root -p < src/main/resources/db/schema.sql
            echo "✅ 数据库初始化完成"
        fi
    else
        echo "⚠️  警告: 无法连接到MySQL"
    fi
else
    echo "⚠️  警告: 未找到MySQL客户端"
fi
echo ""

# 检查Redis
echo "5. 检查Redis连接..."
if command -v redis-cli &> /dev/null; then
    redis-cli ping > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "✅ Redis连接成功"
    else
        echo "⚠️  警告: 无法连接到Redis，请启动Redis服务"
    fi
else
    echo "⚠️  警告: 未找到Redis客户端"
fi
echo ""

# 编译项目
echo "6. 编译项目..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi
echo "✅ 编译成功"
echo ""

# 启动应用
echo "7. 启动应用..."
echo "==================================="
echo ""

mvn spring-boot:run
