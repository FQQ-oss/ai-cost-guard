package com.aicostguard.module.apikey.service.impl;

import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.common.utils.AESUtils;
import com.aicostguard.module.apikey.dto.LlmKeyCreateRequest;
import com.aicostguard.module.apikey.entity.LlmKeyPool;
import com.aicostguard.module.apikey.mapper.LlmKeyPoolMapper;
import com.aicostguard.module.apikey.service.LlmKeyPoolService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LlmKeyPoolServiceImpl implements LlmKeyPoolService {

    private final LlmKeyPoolMapper llmKeyPoolMapper;

    @Override
    public void addKey(LlmKeyCreateRequest request) {
        LlmKeyPool key = new LlmKeyPool();
        key.setProvider(request.getProvider());
        key.setKeyName(request.getKeyName());
        key.setApiKeyEncrypted(AESUtils.encrypt(request.getApiKey()));
        key.setBaseUrl(request.getBaseUrl());
        key.setRpmLimit(request.getRpmLimit());
        key.setTpmLimit(request.getTpmLimit());
        key.setStatus(1);
        llmKeyPoolMapper.insert(key);
    }

    @Override
    public void updateKey(Long id, LlmKeyCreateRequest request) {
        LlmKeyPool key = llmKeyPoolMapper.selectById(id);
        if (key == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Key不存在");
        }
        key.setProvider(request.getProvider());
        key.setKeyName(request.getKeyName());
        if (StringUtils.hasText(request.getApiKey())) {
            key.setApiKeyEncrypted(AESUtils.encrypt(request.getApiKey()));
        }
        key.setBaseUrl(request.getBaseUrl());
        key.setRpmLimit(request.getRpmLimit());
        key.setTpmLimit(request.getTpmLimit());
        llmKeyPoolMapper.updateById(key);
    }

    @Override
    public void deleteKey(Long id) {
        llmKeyPoolMapper.deleteById(id);
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        LlmKeyPool key = llmKeyPoolMapper.selectById(id);
        if (key == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Key不存在");
        }
        key.setStatus(status);
        llmKeyPoolMapper.updateById(key);
    }

    @Override
    public IPage<LlmKeyPool> pageKeys(Page<LlmKeyPool> page, String provider) {
        LambdaQueryWrapper<LlmKeyPool> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(provider)) {
            wrapper.eq(LlmKeyPool::getProvider, provider);
        }
        wrapper.orderByDesc(LlmKeyPool::getCreatedAt);
        IPage<LlmKeyPool> result = llmKeyPoolMapper.selectPage(page, wrapper);
        // 脱敏：只显示前8位
        result.getRecords().forEach(k -> {
            String decrypted = AESUtils.decrypt(k.getApiKeyEncrypted());
            k.setApiKeyEncrypted(decrypted.substring(0, Math.min(8, decrypted.length())) + "****");
        });
        return result;
    }

    @Override
    public List<LlmKeyPool> getAvailableKeys(String provider) {
        return llmKeyPoolMapper.selectList(
                new LambdaQueryWrapper<LlmKeyPool>()
                        .eq(LlmKeyPool::getProvider, provider)
                        .eq(LlmKeyPool::getStatus, 1)
        );
    }

    @Override
    public String decryptKey(Long id) {
        LlmKeyPool key = llmKeyPoolMapper.selectById(id);
        if (key == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Key不存在");
        }
        return AESUtils.decrypt(key.getApiKeyEncrypted());
    }
}
