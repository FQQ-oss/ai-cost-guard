package com.aicostguard.module.apikey.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.module.apikey.dto.LlmKeyCreateRequest;
import com.aicostguard.module.apikey.entity.LlmKeyPool;
import java.util.List;

public interface LlmKeyPoolService {
    void addKey(LlmKeyCreateRequest request);
    void updateKey(Long id, LlmKeyCreateRequest request);
    void deleteKey(Long id);
    void toggleStatus(Long id, Integer status);
    IPage<LlmKeyPool> pageKeys(Page<LlmKeyPool> page, String provider);
    List<LlmKeyPool> getAvailableKeys(String provider);
    String decryptKey(Long id);
}
