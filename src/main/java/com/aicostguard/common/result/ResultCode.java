package com.aicostguard.common.result;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误 1xxx
    USER_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已禁用"),

    // API Key 相关 2xxx
    INVALID_PROXY_KEY(2001, "无效的代理Key"),
    PROXY_KEY_DISABLED(2002, "代理Key已禁用"),
    QUOTA_EXCEEDED(2003, "配额已用完"),

    // 限流相关 3xxx
    RATE_LIMITED(3001, "请求过于频繁"),
    BUDGET_EXCEEDED(3002, "预算已用完"),

    // 代理相关 4xxx
    UPSTREAM_ERROR(4001, "上游服务调用失败"),
    MODEL_NOT_AVAILABLE(4002, "模型不可用"),
    NO_AVAILABLE_KEY(4003, "没有可用的API Key");

    private final int code;
    private final String msg;
}
