package com.aicostguard.module.ratelimit.pipeline;

import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.module.proxy.dto.ProxyContext;
import com.aicostguard.module.proxy.pipeline.ProxyFilter;
import com.aicostguard.module.proxy.pipeline.ProxyFilterChain;
import com.aicostguard.module.ratelimit.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements ProxyFilter {

    private final RateLimitService rateLimitService;

    // 默认限流配置：每个代理Key每分钟60次
    private static final int DEFAULT_MAX_REQUESTS = 60;
    private static final int DEFAULT_WINDOW_SECONDS = 60;

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) {
        String proxyKey = context.getProxyKey();

        boolean allowed = rateLimitService.isAllowed(
                proxyKey, DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_SECONDS
        );

        if (!allowed) {
            throw new BusinessException(ResultCode.RATE_LIMITED,
                    "请求过于频繁，请稍后重试（限制：" + DEFAULT_MAX_REQUESTS + "次/分钟）");
        }

        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 2; // AuthFilter(1) 之后, QuotaFilter(3) 之前
    }
}
