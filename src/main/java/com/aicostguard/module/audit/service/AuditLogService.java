package com.aicostguard.module.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.module.audit.entity.AuditLog;

public interface AuditLogService {

    IPage<AuditLog> pageLogs(Page<AuditLog> page, Long userId, String action);
}
