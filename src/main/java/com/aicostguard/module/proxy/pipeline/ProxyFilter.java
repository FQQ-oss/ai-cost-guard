package com.aicostguard.module.proxy.pipeline;

import com.aicostguard.module.proxy.dto.ProxyContext;

public interface ProxyFilter {
    void doFilter(ProxyContext context, ProxyFilterChain chain);
    int getOrder();
}
