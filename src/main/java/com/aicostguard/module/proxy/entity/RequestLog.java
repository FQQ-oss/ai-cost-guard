package com.aicostguard.module.proxy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("request_log")
public class RequestLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String proxyKey;

    private Long projectId;

    private Long teamId;

    private String provider;

    private String model;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private BigDecimal cost;

    private Integer latencyMs;

    private Integer statusCode;

    private String errorMsg;

    private Integer cacheHit;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
