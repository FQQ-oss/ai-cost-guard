package com.aicostguard.module.proxy.pipeline;

import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.common.utils.AESUtils;
import com.aicostguard.module.apikey.entity.LlmKeyPool;
import com.aicostguard.module.apikey.service.LlmKeyPoolService;
import com.aicostguard.module.proxy.dto.ProxyContext;
import com.aicostguard.module.router.entity.RouteRule;
import com.aicostguard.module.router.service.RouteRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class RouterFilter implements ProxyFilter {

    private final LlmKeyPoolService llmKeyPoolService;
    private final RouteRuleService routeRuleService;
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) {
        String requestModel = context.getChatRequest().getModel();
        int promptLength = context.getChatRequest().getMessages().stream()
                .mapToInt(m -> m.getContent() != null ? m.getContent().length() : 0)
                .sum();

        // 尝试匹配路由规则
        RouteRule rule = routeRuleService.matchRule(requestModel, promptLength);
        String provider;
        String model;
        if (rule != null) {
            provider = rule.getTargetProvider();
            model = rule.getTargetModel();
        } else {
            provider = inferProvider(requestModel);
            model = requestModel;
        }

        context.setTargetProvider(provider);
        context.setTargetModel(model);

        // 获取可用 Key，Round-Robin
        List<LlmKeyPool> keys = llmKeyPoolService.getAvailableKeys(provider);
        if (keys.isEmpty()) {
            // 如果有降级规则，尝试降级
            if (rule != null && rule.getFallbackProvider() != null) {
                provider = rule.getFallbackProvider();
                model = rule.getFallbackModel();
                context.setTargetProvider(provider);
                context.setTargetModel(model);
                keys = llmKeyPoolService.getAvailableKeys(provider);
            }
            if (keys.isEmpty()) {
                throw new BusinessException(ResultCode.NO_AVAILABLE_KEY);
            }
        }

        // Round-Robin 选择 Key
        int idx = Math.abs(counter.getAndIncrement() % keys.size());
        LlmKeyPool selectedKey = keys.get(idx);
        context.setSelectedKey(selectedKey);
        context.setDecryptedApiKey(AESUtils.decrypt(selectedKey.getApiKeyEncrypted()));

        chain.doFilter(context);
    }

    private String inferProvider(String model) {
        if (model == null) return "openai";
        if (model.startsWith("gpt") || model.startsWith("o1") || model.startsWith("o3")) return "openai";
        if (model.startsWith("claude")) return "claude";
        if (model.startsWith("deepseek")) return "deepseek";
        return "openai";
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
