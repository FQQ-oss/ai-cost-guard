package com.aicostguard.module.apikey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ModelPricingRequest {
    @NotBlank(message = "厂商不能为空")
    private String provider;
    @NotBlank(message = "模型名称不能为空")
    private String model;
    @NotNull(message = "输入价格不能为空")
    private BigDecimal inputPricePer1k;
    @NotNull(message = "输出价格不能为空")
    private BigDecimal outputPricePer1k;
    private String currency = "USD";
}
