package com.aicostguard.module.stats.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailyStatsVO {
    private LocalDate date;
    private Long requests;
    private Long promptTokens;
    private Long completionTokens;
    private BigDecimal cost;
    private Long cacheHits;
}
