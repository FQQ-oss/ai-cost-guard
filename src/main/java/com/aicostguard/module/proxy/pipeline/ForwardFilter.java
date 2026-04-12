package com.aicostguard.module.proxy.pipeline;

import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.module.apikey.entity.LlmKeyPool;
import com.aicostguard.module.apikey.service.ModelPricingService;
import com.aicostguard.module.apikey.entity.ModelPricing;
import com.aicostguard.module.proxy.dto.ChatRequest;
import com.aicostguard.module.proxy.dto.ChatResponse;
import com.aicostguard.module.proxy.dto.ProxyContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForwardFilter implements ProxyFilter {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final ModelPricingService modelPricingService;

    private static final Map<String, String> DEFAULT_BASE_URLS = Map.of(
            "openai", "https://api.openai.com",
            "claude", "https://api.anthropic.com",
            "deepseek", "https://api.deepseek.com"
    );

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) {
        // 如果缓存命中，跳过转发
        if (context.isCacheHit()) {
            chain.doFilter(context);
            return;
        }

        LlmKeyPool selectedKey = context.getSelectedKey();
        String baseUrl = selectedKey.getBaseUrl() != null ? selectedKey.getBaseUrl()
                : DEFAULT_BASE_URLS.getOrDefault(context.getTargetProvider(), "https://api.openai.com");

        // 统一转成 OpenAI 兼容格式
        String endpoint = "/v1/chat/completions";

        try {
            ChatRequest request = context.getChatRequest();
            request.setStream(false); // Phase 2 先做非流式，Phase 3 加 SSE

            ChatResponse response = webClientBuilder.build()
                    .post()
                    .uri(baseUrl + endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + context.getDecryptedApiKey())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .timeout(Duration.ofSeconds(120))
                    .block();

            if (response != null) {
                context.setChatResponse(response);
                context.setStatusCode(200);

                // 提取 token 用量
                if (response.getUsage() != null) {
                    context.setPromptTokens(response.getUsage().getPromptTokens());
                    context.setCompletionTokens(response.getUsage().getCompletionTokens());
                }

                // 计算费用
                calculateCost(context);
            }
        } catch (WebClientResponseException e) {
            context.setStatusCode(e.getStatusCode().value());
            context.setErrorMsg(e.getResponseBodyAsString());
            log.error("LLM API调用失败: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ResultCode.UPSTREAM_ERROR, "上游API调用失败: " + e.getMessage());
        } catch (Exception e) {
            context.setStatusCode(500);
            context.setErrorMsg(e.getMessage());
            log.error("代理转发异常", e);
            throw new BusinessException(ResultCode.UPSTREAM_ERROR, "代理转发异常: " + e.getMessage());
        }

        context.setLatencyMs(System.currentTimeMillis() - context.getStartTime());
        chain.doFilter(context);
    }

    private void calculateCost(ProxyContext context) {
        ModelPricing pricing = modelPricingService.getPricing(context.getTargetProvider(), context.getTargetModel());
        if (pricing != null) {
            BigDecimal inputCost = pricing.getInputPricePer1k()
                    .multiply(BigDecimal.valueOf(context.getPromptTokens()))
                    .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
            BigDecimal outputCost = pricing.getOutputPricePer1k()
                    .multiply(BigDecimal.valueOf(context.getCompletionTokens()))
                    .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
            context.setCost(inputCost.add(outputCost));
        }
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
