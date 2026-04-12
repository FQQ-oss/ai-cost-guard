package com.aicostguard.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.module.system.dto.TeamCreateRequest;
import com.aicostguard.module.system.entity.SysTeam;

import java.util.List;

public interface SysTeamService {
    void createTeam(TeamCreateRequest request, Long userId);
    void updateTeam(Long id, TeamCreateRequest request);
    void deleteTeam(Long id);
    SysTeam getTeamById(Long id);
    IPage<SysTeam> pageTeams(Page<SysTeam> page, String keyword);
    void addMember(Long teamId, Long userId, String role);
    void removeMember(Long teamId, Long userId);
    List<SysTeam> getTeamsByUserId(Long userId);
}
