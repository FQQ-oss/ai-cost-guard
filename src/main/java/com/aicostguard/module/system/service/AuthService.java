package com.aicostguard.module.system.service;

import com.aicostguard.module.system.dto.LoginRequest;
import com.aicostguard.module.system.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void logout(Long userId);
}
