package com.aicostguard.module.apikey.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("project_api_key")
public class ProjectApiKey {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String proxyKey;
    private Long quotaTokens;
    private BigDecimal quotaAmount;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
