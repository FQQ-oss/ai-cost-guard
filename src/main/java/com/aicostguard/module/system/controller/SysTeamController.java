package com.aicostguard.module.system.controller;

import com.aicostguard.common.result.Result;
import com.aicostguard.module.system.dto.TeamCreateRequest;
import com.aicostguard.module.system.entity.SysTeam;
import com.aicostguard.module.system.service.SysTeamService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/teams")
@RequiredArgsConstructor
public class SysTeamController {

    private final SysTeamService teamService;

    @PostMapping
    public Result<Void> create(@Valid @RequestBody TeamCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        teamService.createTeam(request, userId);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TeamCreateRequest request) {
        teamService.updateTeam(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SysTeam> getById(@PathVariable Long id) {
        return Result.success(teamService.getTeamById(id));
    }

    @GetMapping
    public Result<IPage<SysTeam>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(teamService.pageTeams(new Page<>(current, size), keyword));
    }

    @PostMapping("/{teamId}/members/{userId}")
    public Result<Void> addMember(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "member") String role) {
        teamService.addMember(teamId, userId, role);
        return Result.success();
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long teamId, @PathVariable Long userId) {
        teamService.removeMember(teamId, userId);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<SysTeam>> myTeams(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(teamService.getTeamsByUserId(userId));
    }
}
