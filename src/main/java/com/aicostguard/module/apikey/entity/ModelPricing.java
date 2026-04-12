package com.aicostguard.module.apikey.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("model_pricing")
public class ModelPricing {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String provider;
    private String model;
    private BigDecimal inputPricePer1k;
    private BigDecimal outputPricePer1k;
    private String currency;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
