package com.aicostguard.module.cache.service;

import com.aicostguard.module.cache.dto.CacheConfigRequest;
import com.aicostguard.module.cache.entity.CacheConfig;

public interface CacheConfigService {
    void saveConfig(CacheConfigRequest request);
    CacheConfig getByProjectId(Long projectId);
}
