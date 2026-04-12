package com.aicostguard.module.proxy.controller;

import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.module.proxy.dto.ChatRequest;
import com.aicostguard.module.proxy.dto.ChatResponse;
import com.aicostguard.module.proxy.dto.ProxyContext;
import com.aicostguard.module.proxy.pipeline.ProxyPipeline;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProxyController {

    private final ProxyPipeline proxyPipeline;

    @PostMapping("/chat/completions")
    public ResponseEntity<ChatResponse> chatCompletions(
            @RequestHeader("Authorization") String authorization,
            @RequestBody ChatRequest request) {

        // 提取代理 Key
        String proxyKey = extractProxyKey(authorization);

        // 构建上下文
        ProxyContext context = new ProxyContext();
        context.setProxyKey(proxyKey);
        context.setChatRequest(request);
        context.setStream(Boolean.TRUE.equals(request.getStream()));

        // 执行 Pipeline
        proxyPipeline.execute(context);

        return ResponseEntity.ok(context.getChatResponse());
    }

    private String extractProxyKey(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "缺少Authorization头");
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
