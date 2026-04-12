CREATE DATABASE IF NOT EXISTS ai_cost_guard DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ai_cost_guard;

-- 用户表
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

-- 角色表
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '描述',
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '角色表';

-- 用户角色关联
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT '用户角色关联表';

-- 团队表
CREATE TABLE sys_team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL COMMENT '团队名称',
    description VARCHAR(500) COMMENT '描述',
    owner_id BIGINT COMMENT '负责人ID',
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '团队表';

-- 团队成员
CREATE TABLE sys_team_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'member' COMMENT '团队角色 owner/admin/member',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_user (team_id, user_id)
) COMMENT '团队成员表';

-- 项目表
CREATE TABLE project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL COMMENT '所属团队',
    project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description VARCHAR(500) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '0-禁用 1-启用',
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_team_id (team_id)
) COMMENT '项目表';

-- LLM Key 池
CREATE TABLE llm_key_pool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider VARCHAR(30) NOT NULL COMMENT '厂商 openai/claude/deepseek',
    key_name VARCHAR(100) COMMENT 'Key 备注名',
    api_key_encrypted TEXT NOT NULL COMMENT 'AES加密后的Key',
    base_url VARCHAR(255) COMMENT 'API 基础URL',
    rpm_limit INT DEFAULT 60 COMMENT '每分钟请求限制',
    tpm_limit INT DEFAULT 100000 COMMENT '每分钟Token限制',
    status TINYINT DEFAULT 1 COMMENT '0-禁用 1-启用',
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT 'LLM API Key池';

-- 项目代理 Key
CREATE TABLE project_api_key (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    proxy_key VARCHAR(100) NOT NULL UNIQUE COMMENT '代理Key (acg-xxxx)',
    quota_tokens BIGINT DEFAULT -1 COMMENT '月度Token配额 -1不限',
    quota_amount DECIMAL(10,4) DEFAULT -1 COMMENT '月度金额配额 -1不限',
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project_id (project_id),
    INDEX idx_proxy_key (proxy_key)
) COMMENT '项目代理Key';

-- 模型定价
CREATE TABLE model_pricing (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider VARCHAR(30) NOT NULL,
    model VARCHAR(50) NOT NULL COMMENT '模型名称',
    input_price_per_1k DECIMAL(10,6) NOT NULL COMMENT '输入价格/1K tokens',
    output_price_per_1k DECIMAL(10,6) NOT NULL COMMENT '输出价格/1K tokens',
    currency VARCHAR(10) DEFAULT 'USD',
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_provider_model (provider, model)
) COMMENT '模型定价表';

-- 路由规则
CREATE TABLE route_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    condition_expr VARCHAR(500) COMMENT 'SpEL条件表达式',
    target_provider VARCHAR(30) NOT NULL,
    target_model VARCHAR(50) NOT NULL,
    priority INT DEFAULT 0 COMMENT '优先级(越大越先匹配)',
    fallback_provider VARCHAR(30) COMMENT '降级厂商',
    fallback_model VARCHAR(50) COMMENT '降级模型',
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '路由规则表';

-- 请求日志（大表，后续分区）
CREATE TABLE request_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    proxy_key VARCHAR(100) COMMENT '代理Key',
    project_id BIGINT COMMENT '项目ID',
    team_id BIGINT COMMENT '团队ID',
    provider VARCHAR(30) COMMENT '厂商',
    model VARCHAR(50) COMMENT '模型',
    prompt_tokens INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    total_tokens INT DEFAULT 0,
    cost DECIMAL(10,6) DEFAULT 0 COMMENT '本次花费',
    latency_ms INT DEFAULT 0 COMMENT '延迟毫秒',
    status_code INT COMMENT 'HTTP状态码',
    error_msg VARCHAR(500) COMMENT '错误信息',
    cache_hit TINYINT DEFAULT 0 COMMENT '是否命中缓存',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_project_date (project_id, created_at),
    INDEX idx_proxy_key (proxy_key),
    INDEX idx_created_at (created_at)
) COMMENT '请求日志表';

-- 日用量统计
CREATE TABLE usage_stats_daily (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    provider VARCHAR(30) NOT NULL,
    model VARCHAR(50) NOT NULL,
    stat_date DATE NOT NULL,
    total_requests INT DEFAULT 0,
    total_prompt_tokens BIGINT DEFAULT 0,
    total_completion_tokens BIGINT DEFAULT 0,
    total_cost DECIMAL(12,6) DEFAULT 0,
    cache_hit_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stats (project_id, provider, model, stat_date)
) COMMENT '日用量统计表';

-- 缓存配置
CREATE TABLE cache_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL UNIQUE,
    enabled TINYINT DEFAULT 0,
    similarity_threshold INT DEFAULT 3 COMMENT 'SimHash海明距离阈值',
    ttl_seconds INT DEFAULT 3600 COMMENT '缓存过期秒数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '缓存配置表';

-- 告警规则
CREATE TABLE alert_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,
    alert_type VARCHAR(30) NOT NULL COMMENT 'budget/spike/error_rate',
    project_id BIGINT COMMENT '关联项目，NULL表示全局',
    condition_expr VARCHAR(500) COMMENT '条件',
    threshold DECIMAL(10,2) COMMENT '阈值',
    notify_channels VARCHAR(200) COMMENT '通知渠道JSON',
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '告警规则表';

-- 告警历史
CREATE TABLE alert_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    alert_content TEXT COMMENT '告警内容',
    notify_status TINYINT DEFAULT 0 COMMENT '0-未通知 1-已通知 2-通知失败',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rule_id (rule_id),
    INDEX idx_created_at (created_at)
) COMMENT '告警历史表';

-- 审计日志
CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '操作人',
    username VARCHAR(50) COMMENT '操作人用户名',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource VARCHAR(100) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    detail TEXT COMMENT '详情',
    ip VARCHAR(50) COMMENT 'IP地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) COMMENT '审计日志表';

-- 初始数据
INSERT INTO sys_role (role_code, role_name, description) VALUES
('admin', '管理员', '系统管理员'),
('team_lead', '团队负责人', '团队负责人'),
('developer', '开发者', '普通开发者');

INSERT INTO sys_user (username, password, nickname, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'Admin', 1);

INSERT INTO model_pricing (provider, model, input_price_per_1k, output_price_per_1k) VALUES
('openai', 'gpt-4o', 0.005, 0.015),
('openai', 'gpt-4o-mini', 0.00015, 0.0006),
('openai', 'gpt-3.5-turbo', 0.0005, 0.0015),
('claude', 'claude-sonnet-4-20250514', 0.003, 0.015),
('claude', 'claude-haiku-4-5-20251001', 0.001, 0.005),
('claude', 'claude-opus-4-20250514', 0.015, 0.075),
('deepseek', 'deepseek-chat', 0.00014, 0.00028),
('deepseek', 'deepseek-reasoner', 0.00055, 0.0022);
