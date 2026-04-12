package com.aicostguard.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamCreateRequest {
    @NotBlank(message = "团队名称不能为空")
    private String teamName;
    private String description;
}
