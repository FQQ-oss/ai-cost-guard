package com.aicostguard.module.proxy.pipeline;

import com.aicostguard.module.proxy.dto.ProxyContext;
import java.util.List;

public class ProxyFilterChain {
    private final List<ProxyFilter> filters;
    private int index = 0;

    public ProxyFilterChain(List<ProxyFilter> filters) {
        this.filters = filters;
    }

    public void doFilter(ProxyContext context) {
        if (index < filters.size()) {
            ProxyFilter filter = filters.get(index++);
            filter.doFilter(context, this);
        }
    }
}
