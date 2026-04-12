package com.aicostguard.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_team_member")
public class SysTeamMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teamId;
    private Long userId;
    private String role;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
