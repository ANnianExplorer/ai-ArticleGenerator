<div align="center">

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]

### AI 爆款文章创作器

*基于 Spring AI Alibaba 的企业级 AI 写作平台，支持多智能体编排配图生成*

</div>

## ✨ 核心功能

|     模块      | 功能描述                                        |
|:-----------:|----------------------------------------------|
| **标题生成**  | AI 根据选题生成 5 组标题方案（主标题 + 副标题）             |
| **大纲生成**  | AI 生成章节大纲，支持 AI 智能修改                     |
| **正文生成**  | AI 撰写完整文章，支持 Markdown                    |
| **配图生成**  | 多图源支持：Pexels、Nano Banana、Emoji、SVG、Mermaid |
| **实时进度**  | SSE 流式推送，实时显示生成进度                     |
| **用户管理**  | 认证授权、配额系统、VIP 会员                       |
| **数据统计**  | 使用分析、图表展示                               |

## 🚀 快速开始

### 环境要求

| 依赖      | 版本要求   |
|---------|----------|
| MySQL   | 8.0+    |
| Redis   | 6.0+    |
| Node.js | 22+     |
| Maven   | 3.9+    |
| Java    | 21      |

### 本地启动

```bash
# 1. 初始化数据库
mysql -uroot -p123456 < sql/create_table.sql

# 2. 启动后端
mvn spring-boot:run

# 3. 启动前端
cd frontend
npm install
npm run dev
```

### 服务端口

| 服务     | 端口   | 说明        |
|---------|-------|------------|
| 后端 API | 8567  | /api context |
| 前端    | 5173  | Vite dev   |
| MySQL   | 3306  | 数据库     |
| Redis   | 6379  | 缓存       |

## 🛠️ 技术架构

### 核心框架
- **后端**：Spring Boot 3.5.9 + Spring AI Alibaba + MyBatis-Flex
- **前端**：Vue 3 + Vite + Ant Design Vue + Pinia
- **数据**：MySQL 8.0 + Redis
- **AI**：DashScope + Pexels + Gemini (Nano Banana)

### 技术特点
- 多智能体编排（Spring AI Alibaba Agent Framework）
- SSE 实时通信
- Redis Session
- 腾讯云 COS 图片存储

## 📦 项目模块

| 模块              | 说明            |
|------------------|----------------|
| src/main/java    | 后端 Java 源码  |
| frontend/        | Vue 3 前端项目   |
| sql/             | 数据库脚本      |
| src/main/resources | 配置文件      |

## 📚 API 文档

启动后访问：`http://localhost:8567/api/doc.html`

## 🤝 参与贡献

1. **Fork** 项目到您的账户
2. **创建分支** (`git checkout -b feature/xxx`)
3. **提交代码** (`git commit -m 'add xxx'`)
4. **推送分支** (`git push origin feature/xxx`)
5. **发起 Pull Request**

## 📄 开源协议

本项目采用 **MIT** 开源协议。

## 🙏 特别鸣谢

- [Spring AI Alibaba](https://github.com/spring-ai-alibaba) - AI 智能体框架
- [MyBatis-Flex](https://github.com/mybatis-flex/mybatis-flex) - MyBatis 增强框架
- [Ant Design Vue](https://github.com/vueComponent/ant-design-vue) - Vue UI 组件库

---

<!-- Badge Links -->

[contributors-shield]: https://img.shields.io/badge/contributors-1-brightgreen

[contributors-url]: #

[forks-shield]: https://img.shields.io/badge/forks-1-brightgreen

[forks-url]: #

[stars-shield]: https://img.shields.io/badge/stars-1-brightgreen

[stars-url]: #

[issues-shield]: https://img.shields.io/badge/issues-1-brightgreen

[issues-url]: #

[license-shield]: https://img.shields.io/badge/license-MIT-brightgreen

[license-url]: #