package com.aicostguard.module.cache.service;

public interface SemanticCacheService {
    /**
     * 尝试从缓存获取响应
     * @param projectId 项目ID
     * @param prompt 用户prompt
     * @return 缓存的响应JSON，null表示未命中
     */
    String getFromCache(Long projectId, String prompt);

    /**
     * 写入缓存
     * @param projectId 项目ID
     * @param prompt 用户prompt
     * @param response 响应JSON
     */
    void putToCache(Long projectId, String prompt, String response);
}
