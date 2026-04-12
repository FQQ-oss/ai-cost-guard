package com.aicostguard.module.apikey.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("llm_key_pool")
public class LlmKeyPool {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String provider;
    private String keyName;
    private String apiKeyEncrypted;
    private String baseUrl;
    private Integer rpmLimit;
    private Integer tpmLimit;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
