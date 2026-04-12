package com.aicostguard.module.router.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.common.result.Result;
import com.aicostguard.module.router.dto.RouteRuleRequest;
import com.aicostguard.module.router.entity.RouteRule;
import com.aicostguard.module.router.service.RouteRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/router/rules")
@RequiredArgsConstructor
public class RouteRuleController {

    private final RouteRuleService routeRuleService;

    @PostMapping
    public Result<Void> create(@Valid @RequestBody RouteRuleRequest request) {
        routeRuleService.createRule(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RouteRuleRequest request) {
        routeRuleService.updateRule(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        routeRuleService.deleteRule(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        routeRuleService.toggleStatus(id, status);
        return Result.success();
    }

    @GetMapping
    public Result<IPage<RouteRule>> page(@RequestParam(defaultValue = "1") Integer current,
                                         @RequestParam(defaultValue = "10") Integer size) {
        Page<RouteRule> page = new Page<>(current, size);
        return Result.success(routeRuleService.pageRules(page));
    }
}
