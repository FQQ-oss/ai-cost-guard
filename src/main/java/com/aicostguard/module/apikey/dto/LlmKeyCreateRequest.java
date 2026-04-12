package com.aicostguard.module.apikey.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LlmKeyCreateRequest {
    @NotBlank(message = "厂商不能为空")
    private String provider;
    private String keyName;
    @NotBlank(message = "API Key不能为空")
    private String apiKey;
    private String baseUrl;
    private Integer rpmLimit = 60;
    private Integer tpmLimit = 100000;
}
