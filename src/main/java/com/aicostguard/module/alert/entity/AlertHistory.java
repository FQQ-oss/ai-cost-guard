package com.aicostguard.module.alert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alert_history")
public class AlertHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ruleId;

    private String alertContent;

    private Integer notifyStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
