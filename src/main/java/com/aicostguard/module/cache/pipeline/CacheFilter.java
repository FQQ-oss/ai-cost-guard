package com.aicostguard.module.cache.pipeline;

import com.aicostguard.module.cache.service.SemanticCacheService;
import com.aicostguard.module.proxy.dto.ChatRequest;
import com.aicostguard.module.proxy.dto.ChatResponse;
import com.aicostguard.module.proxy.dto.ProxyContext;
import com.aicostguard.module.proxy.pipeline.ProxyFilter;
import com.aicostguard.module.proxy.pipeline.ProxyFilterChain;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheFilter implements ProxyFilter {

    private final SemanticCacheService semanticCacheService;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) {
        // 流式请求不走缓存
        if (context.isStream()) {
            chain.doFilter(context);
            return;
        }

        Long projectId = context.getProjectId();
        if (projectId == null) {
            chain.doFilter(context);
            return;
        }

        // 提取 prompt 文本
        String promptText = extractPrompt(context.getChatRequest());

        // 查缓存
        String cached = semanticCacheService.getFromCache(projectId, promptText);
        if (cached != null) {
            try {
                ChatResponse response = objectMapper.readValue(cached, ChatResponse.class);
                context.setChatResponse(response);
                context.setCacheHit(true);
                context.setStatusCode(200);
                context.setLatencyMs(System.currentTimeMillis() - context.getStartTime());
                // 缓存命中，继续链（后续 ForwardFilter 会检查 cacheHit 跳过转发）
                chain.doFilter(context);
                return;
            } catch (Exception e) {
                log.warn("缓存反序列化失败", e);
            }
        }

        // 缓存未命中，继续链
        chain.doFilter(context);

        // 请求完成后，写入缓存
        if (context.getStatusCode() == 200 && context.getChatResponse() != null && !context.isCacheHit()) {
            try {
                String responseJson = objectMapper.writeValueAsString(context.getChatResponse());
                semanticCacheService.putToCache(projectId, promptText, responseJson);
            } catch (Exception e) {
                log.warn("写入语义缓存失败", e);
            }
        }
    }

    private String extractPrompt(ChatRequest request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return "";
        }
        return request.getMessages().stream()
                .map(ChatRequest.Message::getContent)
                .filter(c -> c != null)
                .collect(Collectors.joining("\n"));
    }

    @Override
    public int getOrder() {
        return 4; // 在 QuotaFilter(3) 之后，RouterFilter(5) 之前
    }
}
