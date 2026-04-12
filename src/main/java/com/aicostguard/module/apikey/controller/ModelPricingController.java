package com.aicostguard.module.apikey.controller;

import com.aicostguard.common.result.Result;
import com.aicostguard.module.apikey.dto.ModelPricingRequest;
import com.aicostguard.module.apikey.entity.ModelPricing;
import com.aicostguard.module.apikey.service.ModelPricingService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apikey/pricing")
@RequiredArgsConstructor
public class ModelPricingController {

    private final ModelPricingService modelPricingService;

    @PostMapping
    public Result<Void> add(@Valid @RequestBody ModelPricingRequest request) {
        modelPricingService.addPricing(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ModelPricingRequest request) {
        modelPricingService.updatePricing(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        modelPricingService.deletePricing(id);
        return Result.success();
    }

    @GetMapping
    public Result<IPage<ModelPricing>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String provider) {
        return Result.success(modelPricingService.pagePricing(new Page<>(current, size), provider));
    }
}
