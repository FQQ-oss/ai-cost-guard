package com.aicostguard.module.proxy.dto;

import com.aicostguard.module.apikey.entity.LlmKeyPool;
import com.aicostguard.module.apikey.entity.ProjectApiKey;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProxyContext {
    // 认证信息
    private String proxyKey;
    private ProjectApiKey projectApiKey;
    private Long projectId;
    private Long teamId;

    // 请求信息
    private ChatRequest chatRequest;
    private String targetProvider;
    private String targetModel;

    // 路由选择的 Key
    private LlmKeyPool selectedKey;
    private String decryptedApiKey;

    // 缓存
    private boolean cacheHit;
    private String cachedResponse;

    // 响应信息
    private ChatResponse chatResponse;
    private int promptTokens;
    private int completionTokens;
    private BigDecimal cost = BigDecimal.ZERO;
    private long latencyMs;
    private int statusCode = 200;
    private String errorMsg;

    // 是否流式
    private boolean stream;

    private long startTime = System.currentTimeMillis();
}
