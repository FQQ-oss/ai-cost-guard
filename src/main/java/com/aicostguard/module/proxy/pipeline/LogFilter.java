package com.aicostguard.module.proxy.pipeline;

import com.aicostguard.module.proxy.dto.ProxyContext;
import com.aicostguard.module.proxy.entity.RequestLog;
import com.aicostguard.module.proxy.mapper.RequestLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogFilter implements ProxyFilter {

    private final RequestLogMapper requestLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) {
        // LogFilter 是最后一个，先继续链（虽然后面没有了）
        chain.doFilter(context);
        // 异步记录
        asyncLog(context);
    }

    private void asyncLog(ProxyContext context) {
        try {
            // 1. 写入请求日志
            RequestLog logEntry = new RequestLog();
            logEntry.setProxyKey(context.getProxyKey());
            logEntry.setProjectId(context.getProjectId());
            logEntry.setTeamId(context.getTeamId());
            logEntry.setProvider(context.getTargetProvider());
            logEntry.setModel(context.getTargetModel());
            logEntry.setPromptTokens(context.getPromptTokens());
            logEntry.setCompletionTokens(context.getCompletionTokens());
            logEntry.setTotalTokens(context.getPromptTokens() + context.getCompletionTokens());
            logEntry.setCost(context.getCost());
            logEntry.setLatencyMs((int) context.getLatencyMs());
            logEntry.setStatusCode(context.getStatusCode());
            logEntry.setErrorMsg(context.getErrorMsg());
            logEntry.setCacheHit(context.isCacheHit() ? 1 : 0);
            requestLogMapper.insert(logEntry);

            // 2. 更新 Redis 实时统计
            String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int totalTokens = context.getPromptTokens() + context.getCompletionTokens();

            // 累加月度 Token 用量（用于配额检查）
            String tokenBudgetKey = "budget:tokens:" + context.getProjectId() + ":" + month;
            redisTemplate.opsForValue().increment(tokenBudgetKey, totalTokens);
            redisTemplate.expire(tokenBudgetKey, 35, TimeUnit.DAYS);

            // 累加月度金额（用于预算检查）
            String amountBudgetKey = "budget:amount:" + context.getProjectId() + ":" + month;
            redisTemplate.opsForValue().increment(amountBudgetKey, context.getCost().doubleValue());
            redisTemplate.expire(amountBudgetKey, 35, TimeUnit.DAYS);

            // 累加日统计（用于报表）
            String statsKey = "stats:" + context.getProjectId() + ":" + date + ":" + context.getTargetModel();
            redisTemplate.opsForHash().increment(statsKey, "requests", 1);
            redisTemplate.opsForHash().increment(statsKey, "prompt_tokens", context.getPromptTokens());
            redisTemplate.opsForHash().increment(statsKey, "completion_tokens", context.getCompletionTokens());
            redisTemplate.opsForHash().increment(statsKey, "cost", context.getCost().doubleValue());
            if (context.isCacheHit()) {
                redisTemplate.opsForHash().increment(statsKey, "cache_hits", 1);
            }
            redisTemplate.expire(statsKey, 2, TimeUnit.DAYS);

        } catch (Exception e) {
            log.error("记录请求日志失败", e);
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
