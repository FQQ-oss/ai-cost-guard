package com.aicostguard.module.audit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.common.result.Result;
import com.aicostguard.module.audit.entity.AuditLog;
import com.aicostguard.module.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit/logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public Result<?> pageLogs(@RequestParam(defaultValue = "1") long current,
                              @RequestParam(defaultValue = "10") long size,
                              @RequestParam(required = false) Long userId,
                              @RequestParam(required = false) String action) {
        return Result.success(auditLogService.pageLogs(new Page<AuditLog>(current, size), userId, action));
    }
}
