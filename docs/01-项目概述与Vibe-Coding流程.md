# AI Cost Guard - 项目概述与 Vibe Coding 流程

## 一、项目简介

**AI Cost Guard** 是一个 LLM API 成本治理平台，面向公司内部使用。核心功能是作为 LLM API 的代理网关，统一管理多家大模型厂商（OpenAI、Claude、DeepSeek）的 API Key，实现请求代理转发、用量统计、成本核算、预算告警、智能路由和语义缓存。

### 核心价值
- **成本可视化**：实时追踪每个项目、每个模型的 Token 消耗和费用
- **统一网关**：一个代理 Key 访问多家 LLM 厂商，屏蔽底层差异
- **智能路由**：基于 SpEL 表达式的条件路由，自动选择最优模型
- **预算管控**：项目级 Token 配额和金额预算，超限自动拦截
- **语义缓存**：基于 SimHash 相似度的智能缓存，降低重复调用成本

## 二、技术栈

| 层 | 技术 | 版本 |
|---|---|---|
| 后端框架 | Spring Boot | 3.2.5 |
| JDK | Microsoft OpenJDK | 17 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.x |
| 缓存/限流 | Redis + Lettuce | - |
| 认证 | JWT (jjwt) | 0.12.5 |
| 密码加密 | Spring Security Crypto (BCrypt) | - |
| HTTP 转发 | Spring WebFlux WebClient | - |
| 熔断限流 | Resilience4j | 2.2.0 |
| API 文档 | Knife4j (OpenAPI 3) | 4.5.0 |
| 工具库 | Hutool | 5.8.25 |
| 前端框架 | Vue 3 (Composition API) | 3.4+ |
| 构建工具 | Vite | 5.4+ |
| UI 组件库 | Element Plus | 2.7+ |
| 图表 | ECharts | 5.5+ |
| 状态管理 | Pinia | 2.1+ |
| HTTP 客户端 | Axios | 1.7+ |

## 三、Vibe Coding 指令流程

以下是使用 AI 辅助编程（Vibe Coding）从零搭建本项目的完整指令流程。

### 阶段一：项目初始化

**Prompt 1 - 需求定义**
```
我要做一个 LLM API 成本治理平台，叫 AI Cost Guard。
功能包括：
1. 统一代理网关 - 代理转发 OpenAI/Claude/DeepSeek 的 API 请求
2. Key 池管理 - 集中管理多家厂商的 API Key，AES 加密存储
3. 项目管理 - 按项目分配代理 Key，设置 Token 和金额配额
4. 模型定价 - 维护各模型的输入/输出 Token 单价
5. 智能路由 - 基于 SpEL 表达式的条件路由规则
6. 语义缓存 - SimHash 相似度匹配，减少重复调用
7. 滑动窗口限流 - Redis Lua 实现
8. 用量统计 - 日维度聚合，实时 Dashboard
9. 预算告警 - 预算/突增告警，邮件/Webhook 通知
10. 审计日志 - AOP 自动记录操作日志
11. 用户/角色/团队管理 - JWT 认证，RBAC 权限

技术栈：Spring Boot 3.2 + MyBatis-Plus + MySQL + Redis + Vue 3 + Element Plus
请帮我设计数据库表结构。
```

**Prompt 2 - 生成建表 SQL**
```
根据以上设计，生成完整的 MySQL 建表 SQL，包括：
- 16 张表（sys_user, sys_role, sys_user_role, sys_team, sys_team_member, 
  project, llm_key_pool, project_api_key, model_pricing, route_rule, 
  request_log, usage_stats_daily, cache_config, alert_rule, alert_history, audit_log）
- 所有索引
- 初始数据（角色、管理员账号、模型定价）
```

**Prompt 3 - 生成后端项目骨架**
```
使用 Spring Boot 3.2.5 + Java 17 生成项目骨架：
- 包结构按模块划分：common, infrastructure, module.system, module.apikey, 
  module.proxy, module.router, module.cache, module.ratelimit, module.stats, 
  module.alert, module.audit
- 每个模块包含：entity, mapper, service, service.impl, controller, dto
- 统一响应 Result<T>、ResultCode 枚举
- 全局异常处理
- JWT 工具类、AES 加密工具类
- MyBatis-Plus 配置（分页、逻辑删除、自动填充）
- Redis 配置、WebClient 配置、CORS 配置
```

### 阶段二：核心模块实现

**Prompt 4 - 用户认证模块**
```
实现用户认证模块：
- 登录接口 POST /api/auth/login，BCrypt 校验密码，返回 JWT
- 注册接口 POST /api/auth/register
- 登出接口 POST /api/auth/logout，Redis 删除 token
- JWT 拦截器，排除登录/注册/代理接口
- Token 存 Redis，支持强制登出
```

**Prompt 5 - 代理网关管道**
```
实现代理网关，使用责任链模式（ProxyFilter + ProxyFilterChain）：
- AuthFilter(order=1): 校验代理 Key
- RateLimitFilter(order=2): Redis Lua 滑动窗口限流
- QuotaFilter(order=3): 检查 Token/金额配额
- CacheFilter(order=4): SimHash 语义缓存
- RouterFilter(order=5): SpEL 条件路由 + Round-Robin 选 Key
- ForwardFilter(order=6): WebClient 转发到上游 LLM API
- LogFilter(order=100): 记录请求日志，更新 Redis 计数器
```

**Prompt 6 - CRUD 管理模块**
```
实现以下管理模块的完整 CRUD：
- Key 池管理（AES 加密存储，列表脱敏显示）
- 项目管理（生成 acg- 前缀代理 Key）
- 模型定价（Redis 缓存 30min）
- 路由规则（SpEL 条件匹配）
- 告警规则/历史
- 审计日志（AOP @AuditLog 注解自动采集）
- 用户/团队管理
```

**Prompt 7 - 定时任务**
```
实现定时任务：
- StatsFlushTask：每 5 分钟将 Redis 实时统计数据刷入 MySQL usage_stats_daily 表
- AlertCheckTask：每 1 分钟检查告警规则（预算类、突增类），触发后记录历史并通知
```

### 阶段三：前端实现

**Prompt 8 - 前端项目搭建**
```
用 Vue 3 + Vite + Element Plus + ECharts + Pinia 搭建前端：
- 登录页
- Layout 侧边栏导航（9 个菜单）
- Axios 封装（统一 /api 前缀、Bearer token、401 跳转）
- Pinia 存储登录状态
- Vue Router 路由守卫
```

**Prompt 9 - 前端页面**
```
实现所有管理页面：
- Dashboard：4 个统计卡片 + 请求量趋势图（柱状+折线双轴）+ 模型分布饼图
- 项目管理：CRUD 表格 + 生成代理 Key
- Key 池管理：添加/删除/启停 Key
- 模型定价：CRUD 表格
- 路由规则：添加/删除规则
- 用量统计：筛选条件 + 统计卡片 + 趋势图
- 告警管理：规则 CRUD + 告警历史
- 审计日志：分页查询
- 用户管理：添加/删除用户
```

### 阶段四：调试修复

**Prompt 10 - 编译错误修复**
```
修复编译问题：
- 缺少 spring-boot-starter-aop 依赖
- 缺少 spring-security-crypto 依赖
- RouteRuleController 中 Result.ok() 改为 Result.success()
- RouteRuleServiceImpl 中 ResultCode.PARAM_ERROR 改为 NOT_FOUND
```

### 阶段五：待完成

**Prompt 11 - 访问控制（待实现）**
```
实现访问控制方案：
- 方案 A：邀请码/管理员审批注册
- 方案 B：内网 IP 白名单
- 前端 Login.vue 增加注册入口
```
