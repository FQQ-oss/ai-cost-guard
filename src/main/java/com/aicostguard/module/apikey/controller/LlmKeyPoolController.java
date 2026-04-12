package com.aicostguard.module.apikey.controller;

import com.aicostguard.common.result.Result;
import com.aicostguard.module.apikey.dto.LlmKeyCreateRequest;
import com.aicostguard.module.apikey.entity.LlmKeyPool;
import com.aicostguard.module.apikey.service.LlmKeyPoolService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/apikey/pool")
@RequiredArgsConstructor
public class LlmKeyPoolController {

    private final LlmKeyPoolService llmKeyPoolService;

    @PostMapping
    public Result<Void> add(@Valid @RequestBody LlmKeyCreateRequest request) {
        llmKeyPoolService.addKey(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody LlmKeyCreateRequest request) {
        llmKeyPoolService.updateKey(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        llmKeyPoolService.deleteKey(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        llmKeyPoolService.toggleStatus(id, body.get("status"));
        return Result.success();
    }

    @GetMapping
    public Result<IPage<LlmKeyPool>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String provider) {
        return Result.success(llmKeyPoolService.pageKeys(new Page<>(current, size), provider));
    }
}
