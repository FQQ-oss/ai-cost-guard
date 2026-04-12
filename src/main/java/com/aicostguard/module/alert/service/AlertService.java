package com.aicostguard.module.alert.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.module.alert.dto.AlertRuleRequest;
import com.aicostguard.module.alert.entity.AlertRule;
import com.aicostguard.module.alert.entity.AlertHistory;

public interface AlertService {

    void createRule(AlertRuleRequest request);

    void updateRule(Long id, AlertRuleRequest request);

    void deleteRule(Long id);

    void toggleStatus(Long id, Integer status);

    IPage<AlertRule> pageRules(Page<AlertRule> page, String alertType);

    IPage<AlertHistory> pageHistory(Page<AlertHistory> page, Long ruleId);
}
