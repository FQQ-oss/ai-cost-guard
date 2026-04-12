package com.aicostguard.module.cache.service;

public interface SimHashService {
    /**
     * 计算文本的 SimHash 值（64位）
     */
    long computeSimHash(String text);

    /**
     * 计算两个 SimHash 的海明距离
     */
    int hammingDistance(long hash1, long hash2);
}
