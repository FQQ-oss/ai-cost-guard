package com.aicostguard.module.alert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.module.alert.dto.AlertRuleRequest;
import com.aicostguard.module.alert.entity.AlertHistory;
import com.aicostguard.module.alert.entity.AlertRule;
import com.aicostguard.module.alert.mapper.AlertHistoryMapper;
import com.aicostguard.module.alert.mapper.AlertRuleMapper;
import com.aicostguard.module.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertHistoryMapper alertHistoryMapper;

    @Override
    public void createRule(AlertRuleRequest request) {
        AlertRule rule = new AlertRule();
        rule.setRuleName(request.getRuleName());
        rule.setAlertType(request.getAlertType());
        rule.setProjectId(request.getProjectId());
        rule.setConditionExpr(request.getConditionExpr());
        rule.setThreshold(request.getThreshold());
        rule.setNotifyChannels(request.getNotifyChannels());
        rule.setStatus(1);
        alertRuleMapper.insert(rule);
    }

    @Override
    public void updateRule(Long id, AlertRuleRequest request) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "告警规则不存在");
        }
        rule.setRuleName(request.getRuleName());
        rule.setAlertType(request.getAlertType());
        rule.setProjectId(request.getProjectId());
        rule.setConditionExpr(request.getConditionExpr());
        rule.setThreshold(request.getThreshold());
        rule.setNotifyChannels(request.getNotifyChannels());
        alertRuleMapper.updateById(rule);
    }

    @Override
    public void deleteRule(Long id) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "告警规则不存在");
        }
        alertRuleMapper.deleteById(id);
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "告警规则不存在");
        }
        rule.setStatus(status);
        alertRuleMapper.updateById(rule);
    }

    @Override
    public IPage<AlertRule> pageRules(Page<AlertRule> page, String alertType) {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        if (alertType != null && !alertType.isEmpty()) {
            wrapper.eq(AlertRule::getAlertType, alertType);
        }
        wrapper.orderByDesc(AlertRule::getCreatedAt);
        return alertRuleMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<AlertHistory> pageHistory(Page<AlertHistory> page, Long ruleId) {
        LambdaQueryWrapper<AlertHistory> wrapper = new LambdaQueryWrapper<>();
        if (ruleId != null) {
            wrapper.eq(AlertHistory::getRuleId, ruleId);
        }
        wrapper.orderByDesc(AlertHistory::getCreatedAt);
        return alertHistoryMapper.selectPage(page, wrapper);
    }
}
