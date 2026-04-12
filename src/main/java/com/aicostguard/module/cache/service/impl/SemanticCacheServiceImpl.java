package com.aicostguard.module.cache.service.impl;

import com.aicostguard.module.cache.entity.CacheConfig;
import com.aicostguard.module.cache.service.CacheConfigService;
import com.aicostguard.module.cache.service.SemanticCacheService;
import com.aicostguard.module.cache.service.SimHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticCacheServiceImpl implements SemanticCacheService {

    private final SimHashService simHashService;
    private final CacheConfigService cacheConfigService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "sem_cache:";
    private static final String CACHE_INDEX_PREFIX = "sem_index:";

    @Override
    public String getFromCache(Long projectId, String prompt) {
        CacheConfig config = cacheConfigService.getByProjectId(projectId);
        if (config == null || config.getEnabled() != 1) {
            return null;
        }

        long simHash = simHashService.computeSimHash(prompt);
        int threshold = config.getSimilarityThreshold();

        // 查找该项目所有缓存的 simhash
        String indexKey = CACHE_INDEX_PREFIX + projectId;
        Set<Object> cachedHashes = redisTemplate.opsForSet().members(indexKey);
        if (cachedHashes == null || cachedHashes.isEmpty()) {
            return null;
        }

        for (Object hashObj : cachedHashes) {
            long cachedHash = Long.parseLong(hashObj.toString());
            int distance = simHashService.hammingDistance(simHash, cachedHash);
            if (distance <= threshold) {
                String cacheKey = CACHE_KEY_PREFIX + projectId + ":" + cachedHash;
                Object response = redisTemplate.opsForValue().get(cacheKey);
                if (response != null) {
                    log.info("语义缓存命中: projectId={}, distance={}", projectId, distance);
                    return response.toString();
                }
            }
        }
        return null;
    }

    @Override
    public void putToCache(Long projectId, String prompt, String response) {
        CacheConfig config = cacheConfigService.getByProjectId(projectId);
        if (config == null || config.getEnabled() != 1) {
            return;
        }

        long simHash = simHashService.computeSimHash(prompt);
        int ttl = config.getTtlSeconds();

        String cacheKey = CACHE_KEY_PREFIX + projectId + ":" + simHash;
        String indexKey = CACHE_INDEX_PREFIX + projectId;

        redisTemplate.opsForValue().set(cacheKey, response, ttl, TimeUnit.SECONDS);
        redisTemplate.opsForSet().add(indexKey, String.valueOf(simHash));
        redisTemplate.expire(indexKey, ttl, TimeUnit.SECONDS);
    }
}
