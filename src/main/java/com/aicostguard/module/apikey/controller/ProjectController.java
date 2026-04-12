package com.aicostguard.module.apikey.controller;

import com.aicostguard.common.result.Result;
import com.aicostguard.module.apikey.dto.ProjectCreateRequest;
import com.aicostguard.module.apikey.entity.Project;
import com.aicostguard.module.apikey.entity.ProjectApiKey;
import com.aicostguard.module.apikey.service.ProjectService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apikey/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public Result<Void> create(@Valid @RequestBody ProjectCreateRequest request) {
        projectService.createProject(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProjectCreateRequest request) {
        projectService.updateProject(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Long id) {
        return Result.success(projectService.getProjectById(id));
    }

    @GetMapping
    public Result<IPage<Project>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String keyword) {
        return Result.success(projectService.pageProjects(new Page<>(current, size), teamId, keyword));
    }

    @PostMapping("/{projectId}/generate-key")
    public Result<ProjectApiKey> generateKey(@PathVariable Long projectId) {
        return Result.success(projectService.generateProxyKey(projectId));
    }

    @DeleteMapping("/proxy-keys/{proxyKeyId}")
    public Result<Void> revokeKey(@PathVariable Long proxyKeyId) {
        projectService.revokeProxyKey(proxyKeyId);
        return Result.success();
    }
}
