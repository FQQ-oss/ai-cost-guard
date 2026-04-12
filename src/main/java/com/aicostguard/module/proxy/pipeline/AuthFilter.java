package com.aicostguard.module.proxy.pipeline;

import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.module.apikey.entity.ProjectApiKey;
import com.aicostguard.module.apikey.service.ProjectService;
import com.aicostguard.module.proxy.dto.ProxyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFilter implements ProxyFilter {

    private final ProjectService projectService;

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) {
        String proxyKey = context.getProxyKey();
        ProjectApiKey apiKey = projectService.getByProxyKey(proxyKey);
        if (apiKey == null) {
            throw new BusinessException(ResultCode.INVALID_PROXY_KEY);
        }
        if (apiKey.getStatus() != 1) {
            throw new BusinessException(ResultCode.PROXY_KEY_DISABLED);
        }
        context.setProjectApiKey(apiKey);
        context.setProjectId(apiKey.getProjectId());
        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
