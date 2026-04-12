package com.aicostguard.module.apikey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProjectCreateRequest {
    @NotNull(message = "团队ID不能为空")
    private Long teamId;
    @NotBlank(message = "项目名称不能为空")
    private String projectName;
    private String description;
    private Long quotaTokens = -1L;
    private BigDecimal quotaAmount = BigDecimal.valueOf(-1);
}
