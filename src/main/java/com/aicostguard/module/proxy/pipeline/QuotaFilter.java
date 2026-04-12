package com.aicostguard.module.proxy.pipeline;

import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.module.apikey.entity.ProjectApiKey;
import com.aicostguard.module.proxy.dto.ProxyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class QuotaFilter implements ProxyFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) {
        ProjectApiKey apiKey = context.getProjectApiKey();
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 检查 Token 配额
        if (apiKey.getQuotaTokens() > 0) {
            String tokenKey = "budget:tokens:" + context.getProjectId() + ":" + month;
            Object usedObj = redisTemplate.opsForValue().get(tokenKey);
            long usedTokens = usedObj != null ? Long.parseLong(usedObj.toString()) : 0;
            if (usedTokens >= apiKey.getQuotaTokens()) {
                throw new BusinessException(ResultCode.QUOTA_EXCEEDED, "Token配额已用完");
            }
        }

        // 检查金额配额
        if (apiKey.getQuotaAmount().compareTo(BigDecimal.ZERO) > 0) {
            String amountKey = "budget:amount:" + context.getProjectId() + ":" + month;
            Object usedObj = redisTemplate.opsForValue().get(amountKey);
            BigDecimal usedAmount = usedObj != null ? new BigDecimal(usedObj.toString()) : BigDecimal.ZERO;
            if (usedAmount.compareTo(apiKey.getQuotaAmount()) >= 0) {
                throw new BusinessException(ResultCode.BUDGET_EXCEEDED, "金额预算已用完");
            }
        }

        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
