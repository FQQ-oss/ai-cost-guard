package com.aicostguard.module.cache.dto;

import lombok.Data;

@Data
public class CacheConfigRequest {
    private Long projectId;
    private Integer enabled;
    private Integer similarityThreshold = 3;
    private Integer ttlSeconds = 3600;
}
