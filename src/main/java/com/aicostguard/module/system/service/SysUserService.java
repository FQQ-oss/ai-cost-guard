package com.aicostguard.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.module.system.dto.UserCreateRequest;
import com.aicostguard.module.system.dto.UserUpdateRequest;
import com.aicostguard.module.system.entity.SysUser;

public interface SysUserService {
    void createUser(UserCreateRequest request);
    void updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    SysUser getUserById(Long id);
    IPage<SysUser> pageUsers(Page<SysUser> page, String keyword);
}
