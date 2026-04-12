package com.aicostguard.infrastructure.aop;

import com.aicostguard.common.annotation.AuditLog;
import com.aicostguard.module.audit.mapper.AuditLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return result;
            HttpServletRequest request = attrs.getRequest();

            com.aicostguard.module.audit.entity.AuditLog logEntry = new com.aicostguard.module.audit.entity.AuditLog();

            Object userId = request.getAttribute("userId");
            Object username = request.getAttribute("username");
            logEntry.setUserId(userId != null ? (Long) userId : null);
            logEntry.setUsername(username != null ? username.toString() : null);
            logEntry.setAction(auditLog.action());
            logEntry.setResource(auditLog.resource());
            logEntry.setIp(getClientIp(request));

            // 尝试记录参数摘要
            try {
                Object[] args = joinPoint.getArgs();
                if (args.length > 0) {
                    String detail = objectMapper.writeValueAsString(args[0]);
                    if (detail.length() > 1000) {
                        detail = detail.substring(0, 1000) + "...";
                    }
                    logEntry.setDetail(detail);
                }
            } catch (Exception ignored) {}

            auditLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.error("审计日志记录失败", e);
        }

        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
