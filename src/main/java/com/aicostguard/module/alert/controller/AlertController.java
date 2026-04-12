package com.aicostguard.module.alert.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.common.result.Result;
import com.aicostguard.module.alert.dto.AlertRuleRequest;
import com.aicostguard.module.alert.entity.AlertHistory;
import com.aicostguard.module.alert.entity.AlertRule;
import com.aicostguard.module.alert.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping("/rules")
    public Result<Void> createRule(@Valid @RequestBody AlertRuleRequest request) {
        alertService.createRule(request);
        return Result.success();
    }

    @PutMapping("/rules/{id}")
    public Result<Void> updateRule(@PathVariable Long id, @Valid @RequestBody AlertRuleRequest request) {
        alertService.updateRule(id, request);
        return Result.success();
    }

    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        alertService.deleteRule(id);
        return Result.success();
    }

    @PutMapping("/rules/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        alertService.toggleStatus(id, status);
        return Result.success();
    }

    @GetMapping("/rules")
    public Result<?> pageRules(@RequestParam(defaultValue = "1") long current,
                               @RequestParam(defaultValue = "10") long size,
                               @RequestParam(required = false) String alertType) {
        return Result.success(alertService.pageRules(new Page<AlertRule>(current, size), alertType));
    }

    @GetMapping("/history")
    public Result<?> pageHistory(@RequestParam(defaultValue = "1") long current,
                                 @RequestParam(defaultValue = "10") long size,
                                 @RequestParam(required = false) Long ruleId) {
        return Result.success(alertService.pageHistory(new Page<AlertHistory>(current, size), ruleId));
    }
}
