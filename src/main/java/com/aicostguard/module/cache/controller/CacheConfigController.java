package com.aicostguard.module.cache.controller;

import com.aicostguard.common.result.Result;
import com.aicostguard.module.cache.dto.CacheConfigRequest;
import com.aicostguard.module.cache.entity.CacheConfig;
import com.aicostguard.module.cache.service.CacheConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cache/config")
@RequiredArgsConstructor
public class CacheConfigController {

    private final CacheConfigService cacheConfigService;

    @PostMapping
    public Result<Void> save(@RequestBody CacheConfigRequest request) {
        cacheConfigService.saveConfig(request);
        return Result.success();
    }

    @GetMapping("/{projectId}")
    public Result<CacheConfig> getByProject(@PathVariable Long projectId) {
        return Result.success(cacheConfigService.getByProjectId(projectId));
    }
}
