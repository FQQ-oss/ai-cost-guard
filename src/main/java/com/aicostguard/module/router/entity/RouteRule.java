package com.aicostguard.module.router.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("route_rule")
public class RouteRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ruleName;

    private String conditionExpr;

    private String targetProvider;

    private String targetModel;

    private Integer priority;

    private String fallbackProvider;

    private String fallbackModel;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
