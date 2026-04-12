package com.aicostguard.module.router.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.module.router.dto.RouteRuleRequest;
import com.aicostguard.module.router.entity.RouteRule;

import java.util.List;

public interface RouteRuleService {
    void createRule(RouteRuleRequest request);
    void updateRule(Long id, RouteRuleRequest request);
    void deleteRule(Long id);
    void toggleStatus(Long id, Integer status);
    IPage<RouteRule> pageRules(Page<RouteRule> page);
    List<RouteRule> getActiveRules();

    /**
     * 根据请求信息匹配路由规则
     * @param model 请求的模型名
     * @param promptLength prompt字符长度
     * @return 匹配的路由规则，null表示无匹配
     */
    RouteRule matchRule(String model, int promptLength);
}
