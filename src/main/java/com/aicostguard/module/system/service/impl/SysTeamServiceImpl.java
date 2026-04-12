package com.aicostguard.module.system.service.impl;

import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.module.system.dto.TeamCreateRequest;
import com.aicostguard.module.system.entity.SysTeam;
import com.aicostguard.module.system.entity.SysTeamMember;
import com.aicostguard.module.system.mapper.SysTeamMapper;
import com.aicostguard.module.system.mapper.SysTeamMemberMapper;
import com.aicostguard.module.system.service.SysTeamService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysTeamServiceImpl implements SysTeamService {

    private final SysTeamMapper teamMapper;
    private final SysTeamMemberMapper teamMemberMapper;

    @Override
    @Transactional
    public void createTeam(TeamCreateRequest request, Long userId) {
        SysTeam team = new SysTeam();
        team.setTeamName(request.getTeamName());
        team.setDescription(request.getDescription());
        team.setOwnerId(userId);
        teamMapper.insert(team);

        SysTeamMember member = new SysTeamMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setRole("owner");
        teamMemberMapper.insert(member);
    }

    @Override
    public void updateTeam(Long id, TeamCreateRequest request) {
        SysTeam team = teamMapper.selectById(id);
        if (team == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "团队不存在");
        }
        team.setTeamName(request.getTeamName());
        team.setDescription(request.getDescription());
        teamMapper.updateById(team);
    }

    @Override
    public void deleteTeam(Long id) {
        teamMapper.deleteById(id);
    }

    @Override
    public SysTeam getTeamById(Long id) {
        SysTeam team = teamMapper.selectById(id);
        if (team == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "团队不存在");
        }
        return team;
    }

    @Override
    public IPage<SysTeam> pageTeams(Page<SysTeam> page, String keyword) {
        LambdaQueryWrapper<SysTeam> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysTeam::getTeamName, keyword);
        }
        wrapper.orderByDesc(SysTeam::getCreatedAt);
        return teamMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void addMember(Long teamId, Long userId, String role) {
        Long count = teamMemberMapper.selectCount(
                new LambdaQueryWrapper<SysTeamMember>()
                        .eq(SysTeamMember::getTeamId, teamId)
                        .eq(SysTeamMember::getUserId, userId)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该用户已在团队中");
        }
        SysTeamMember member = new SysTeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setRole(role != null ? role : "member");
        teamMemberMapper.insert(member);
    }

    @Override
    public void removeMember(Long teamId, Long userId) {
        teamMemberMapper.delete(
                new LambdaQueryWrapper<SysTeamMember>()
                        .eq(SysTeamMember::getTeamId, teamId)
                        .eq(SysTeamMember::getUserId, userId)
        );
    }

    @Override
    public List<SysTeam> getTeamsByUserId(Long userId) {
        List<SysTeamMember> members = teamMemberMapper.selectList(
                new LambdaQueryWrapper<SysTeamMember>().eq(SysTeamMember::getUserId, userId)
        );
        if (members.isEmpty()) {
            return List.of();
        }
        List<Long> teamIds = members.stream().map(SysTeamMember::getTeamId).toList();
        return teamMapper.selectBatchIds(teamIds);
    }
}
