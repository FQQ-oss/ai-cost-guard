package com.aicostguard.module.apikey.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.module.apikey.dto.ModelPricingRequest;
import com.aicostguard.module.apikey.entity.ModelPricing;

public interface ModelPricingService {
    void addPricing(ModelPricingRequest request);
    void updatePricing(Long id, ModelPricingRequest request);
    void deletePricing(Long id);
    IPage<ModelPricing> pagePricing(Page<ModelPricing> page, String provider);
    ModelPricing getPricing(String provider, String model);
}
