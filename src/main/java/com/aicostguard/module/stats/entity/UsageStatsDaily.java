package com.aicostguard.module.stats.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("usage_stats_daily")
public class UsageStatsDaily {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long teamId;

    private String provider;

    private String model;

    private LocalDate statDate;

    private Integer totalRequests;

    private Long totalPromptTokens;

    private Long totalCompletionTokens;

    private BigDecimal totalCost;

    private Integer cacheHitCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
