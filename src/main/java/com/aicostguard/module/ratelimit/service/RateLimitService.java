package com.aicostguard.module.ratelimit.service;

public interface RateLimitService {
    /**
     * 滑动窗口限流检查
     * @param key 限流维度key（如 proxyKey）
     * @param maxRequests 窗口内最大请求数
     * @param windowSeconds 窗口大小（秒）
     * @return true=允许通过, false=被限流
     */
    boolean isAllowed(String key, int maxRequests, int windowSeconds);
}
