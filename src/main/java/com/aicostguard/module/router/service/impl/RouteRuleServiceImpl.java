package com.aicostguard.module.router.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.module.router.dto.RouteRuleRequest;
import com.aicostguard.module.router.entity.RouteRule;
import com.aicostguard.module.router.mapper.RouteRuleMapper;
import com.aicostguard.module.router.service.RouteRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteRuleServiceImpl implements RouteRuleService {

    private final RouteRuleMapper routeRuleMapper;

    @Override
    public void createRule(RouteRuleRequest request) {
        RouteRule rule = new RouteRule();
        rule.setRuleName(request.getRuleName());
        rule.setConditionExpr(request.getConditionExpr());
        rule.setTargetProvider(request.getTargetProvider());
        rule.setTargetModel(request.getTargetModel());
        rule.setPriority(request.getPriority());
        rule.setFallbackProvider(request.getFallbackProvider());
        rule.setFallbackModel(request.getFallbackModel());
        rule.setStatus(1);
        routeRuleMapper.insert(rule);
    }

    @Override
    public void updateRule(Long id, RouteRuleRequest request) {
        RouteRule rule = routeRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        rule.setRuleName(request.getRuleName());
        rule.setConditionExpr(request.getConditionExpr());
        rule.setTargetProvider(request.getTargetProvider());
        rule.setTargetModel(request.getTargetModel());
        rule.setPriority(request.getPriority());
        rule.setFallbackProvider(request.getFallbackProvider());
        rule.setFallbackModel(request.getFallbackModel());
        routeRuleMapper.updateById(rule);
    }

    @Override
    public void deleteRule(Long id) {
        routeRuleMapper.deleteById(id);
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        RouteRule rule = routeRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        rule.setStatus(status);
        routeRuleMapper.updateById(rule);
    }

    @Override
    public IPage<RouteRule> pageRules(Page<RouteRule> page) {
        LambdaQueryWrapper<RouteRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(RouteRule::getPriority);
        return routeRuleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<RouteRule> getActiveRules() {
        LambdaQueryWrapper<RouteRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RouteRule::getStatus, 1)
               .orderByDesc(RouteRule::getPriority);
        return routeRuleMapper.selectList(wrapper);
    }

    @Override
    public RouteRule matchRule(String model, int promptLength) {
        List<RouteRule> activeRules = getActiveRules();
        for (RouteRule rule : activeRules) {
            if (evaluateCondition(rule.getConditionExpr(), model, promptLength)) {
                return rule;
            }
        }
        return null;
    }

    private boolean evaluateCondition(String conditionExpr, String model, int promptLength) {
        if (conditionExpr == null || conditionExpr.isBlank()) {
            return true; // 无条件，默认匹配
        }
        try {
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("model", model);
            context.setVariable("promptLength", promptLength);
            Boolean result = parser.parseExpression(conditionExpr).getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }
}
