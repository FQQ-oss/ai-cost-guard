package com.aicostguard.module.apikey.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicostguard.module.apikey.dto.ProjectCreateRequest;
import com.aicostguard.module.apikey.entity.Project;
import com.aicostguard.module.apikey.entity.ProjectApiKey;

public interface ProjectService {
    void createProject(ProjectCreateRequest request);
    void updateProject(Long id, ProjectCreateRequest request);
    void deleteProject(Long id);
    Project getProjectById(Long id);
    IPage<Project> pageProjects(Page<Project> page, Long teamId, String keyword);
    ProjectApiKey generateProxyKey(Long projectId);
    void revokeProxyKey(Long proxyKeyId);
    ProjectApiKey getByProxyKey(String proxyKey);
}
