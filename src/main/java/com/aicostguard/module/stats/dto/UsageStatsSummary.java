package com.aicostguard.module.stats.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class UsageStatsSummary {
    private Long totalRequests;
    private Long totalPromptTokens;
    private Long totalCompletionTokens;
    private Long totalTokens;
    private BigDecimal totalCost;
    private Long cacheHitCount;
    private Double cacheHitRate;
}
