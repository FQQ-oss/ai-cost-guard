package com.aicostguard.module.proxy.pipeline;

import com.aicostguard.module.proxy.dto.ProxyContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProxyPipeline {

    private final List<ProxyFilter> filters;
    private List<ProxyFilter> sortedFilters;

    @PostConstruct
    public void init() {
        sortedFilters = new ArrayList<>(filters);
        sortedFilters.sort(Comparator.comparingInt(ProxyFilter::getOrder));
    }

    public void execute(ProxyContext context) {
        ProxyFilterChain chain = new ProxyFilterChain(sortedFilters);
        chain.doFilter(context);
    }
}
