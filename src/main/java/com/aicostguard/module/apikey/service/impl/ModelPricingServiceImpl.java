package com.aicostguard.module.apikey.service.impl;

import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.module.apikey.dto.ModelPricingRequest;
import com.aicostguard.module.apikey.entity.ModelPricing;
import com.aicostguard.module.apikey.mapper.ModelPricingMapper;
import com.aicostguard.module.apikey.service.ModelPricingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ModelPricingServiceImpl implements ModelPricingService {

    private final ModelPricingMapper modelPricingMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PRICING_CACHE = "pricing:";

    @Override
    public void addPricing(ModelPricingRequest request) {
        ModelPricing pricing = new ModelPricing();
        pricing.setProvider(request.getProvider());
        pricing.setModel(request.getModel());
        pricing.setInputPricePer1k(request.getInputPricePer1k());
        pricing.setOutputPricePer1k(request.getOutputPricePer1k());
        pricing.setCurrency(request.getCurrency());
        modelPricingMapper.insert(pricing);
    }

    @Override
    public void updatePricing(Long id, ModelPricingRequest request) {
        ModelPricing pricing = modelPricingMapper.selectById(id);
        if (pricing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "定价不存在");
        }
        pricing.setProvider(request.getProvider());
        pricing.setModel(request.getModel());
        pricing.setInputPricePer1k(request.getInputPricePer1k());
        pricing.setOutputPricePer1k(request.getOutputPricePer1k());
        pricing.setCurrency(request.getCurrency());
        modelPricingMapper.updateById(pricing);
        // 清除缓存
        redisTemplate.delete(PRICING_CACHE + request.getProvider() + ":" + request.getModel());
    }

    @Override
    public void deletePricing(Long id) {
        ModelPricing pricing = modelPricingMapper.selectById(id);
        if (pricing != null) {
            redisTemplate.delete(PRICING_CACHE + pricing.getProvider() + ":" + pricing.getModel());
        }
        modelPricingMapper.deleteById(id);
    }

    @Override
    public IPage<ModelPricing> pagePricing(Page<ModelPricing> page, String provider) {
        LambdaQueryWrapper<ModelPricing> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(provider)) {
            wrapper.eq(ModelPricing::getProvider, provider);
        }
        wrapper.orderByAsc(ModelPricing::getProvider).orderByAsc(ModelPricing::getModel);
        return modelPricingMapper.selectPage(page, wrapper);
    }

    @Override
    public ModelPricing getPricing(String provider, String model) {
        String cacheKey = PRICING_CACHE + provider + ":" + model;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof ModelPricing) {
            return (ModelPricing) cached;
        }

        ModelPricing pricing = modelPricingMapper.selectOne(
                new LambdaQueryWrapper<ModelPricing>()
                        .eq(ModelPricing::getProvider, provider)
                        .eq(ModelPricing::getModel, model)
        );
        if (pricing != null) {
            redisTemplate.opsForValue().set(cacheKey, pricing, 30, TimeUnit.MINUTES);
        }
        return pricing;
    }
}
