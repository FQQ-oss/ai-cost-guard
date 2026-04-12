package com.aicostguard.module.alert.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertRuleRequest {

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "告警类型不能为空")
    private String alertType; // budget / spike / error_rate

    private Long projectId;

    private String conditionExpr;

    private BigDecimal threshold;

    private String notifyChannels; // JSON: ["email","webhook"]
}
