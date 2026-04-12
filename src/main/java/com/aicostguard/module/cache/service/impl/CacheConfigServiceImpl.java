package com.aicostguard.module.cache.service.impl;

import com.aicostguard.module.cache.dto.CacheConfigRequest;
import com.aicostguard.module.cache.entity.CacheConfig;
import com.aicostguard.module.cache.mapper.CacheConfigMapper;
import com.aicostguard.module.cache.service.CacheConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheConfigServiceImpl implements CacheConfigService {

    private final CacheConfigMapper cacheConfigMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "cache_config:";
    private static final long CACHE_TTL_MINUTES = 5;

    @Override
    public void saveConfig(CacheConfigRequest request) {
        LambdaQueryWrapper<CacheConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CacheConfig::getProjectId, request.getProjectId());
        CacheConfig existing = cacheConfigMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setEnabled(request.getEnabled());
            existing.setSimilarityThreshold(request.getSimilarityThreshold());
            existing.setTtlSeconds(request.getTtlSeconds());
            cacheConfigMapper.updateById(existing);
        } else {
            CacheConfig config = new CacheConfig();
            config.setProjectId(request.getProjectId());
            config.setEnabled(request.getEnabled());
            config.setSimilarityThreshold(request.getSimilarityThreshold());
            config.setTtlSeconds(request.getTtlSeconds());
            cacheConfigMapper.insert(config);
        }

        // 清除 Redis 缓存
        redisTemplate.delete(CACHE_KEY_PREFIX + request.getProjectId());
    }

    @Override
    public CacheConfig getByProjectId(Long projectId) {
        String cacheKey = CACHE_KEY_PREFIX + projectId;

        // 先查 Redis
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof CacheConfig) {
            return (CacheConfig) cached;
        }

        // 查数据库
        LambdaQueryWrapper<CacheConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CacheConfig::getProjectId, projectId);
        CacheConfig config = cacheConfigMapper.selectOne(wrapper);

        // 写入 Redis
        if (config != null) {
            redisTemplate.opsForValue().set(cacheKey, config, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }

        return config;
    }
}
