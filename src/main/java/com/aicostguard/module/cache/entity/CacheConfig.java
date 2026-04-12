package com.aicostguard.module.cache.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cache_config")
public class CacheConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Integer enabled;

    private Integer similarityThreshold;

    private Integer ttlSeconds;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
