package com.aicostguard.module.router.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RouteRuleRequest {
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;
    private String conditionExpr;
    @NotBlank(message = "目标厂商不能为空")
    private String targetProvider;
    @NotBlank(message = "目标模型不能为空")
    private String targetModel;
    private Integer priority = 0;
    private String fallbackProvider;
    private String fallbackModel;
}
