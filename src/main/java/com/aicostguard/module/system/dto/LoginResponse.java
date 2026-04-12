package com.aicostguard.module.system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String token;
}
