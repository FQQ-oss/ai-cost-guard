package com.aicostguard.module.apikey.service.impl;

import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.module.apikey.dto.ProjectCreateRequest;
import com.aicostguard.module.apikey.entity.Project;
import com.aicostguard.module.apikey.entity.ProjectApiKey;
import com.aicostguard.module.apikey.mapper.ProjectApiKeyMapper;
import com.aicostguard.module.apikey.mapper.ProjectMapper;
import com.aicostguard.module.apikey.service.ProjectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectApiKeyMapper projectApiKeyMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PROXY_KEY_CACHE = "proxy_key:";

    @Override
    @Transactional
    public void createProject(ProjectCreateRequest request) {
        Project project = new Project();
        project.setTeamId(request.getTeamId());
        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setStatus(1);
        projectMapper.insert(project);
    }

    @Override
    public void updateProject(Long id, ProjectCreateRequest request) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }
        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setTeamId(request.getTeamId());
        projectMapper.updateById(project);
    }

    @Override
    public void deleteProject(Long id) {
        projectMapper.deleteById(id);
    }

    @Override
    public Project getProjectById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }
        return project;
    }

    @Override
    public IPage<Project> pageProjects(Page<Project> page, Long teamId, String keyword) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (teamId != null) {
            wrapper.eq(Project::getTeamId, teamId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Project::getProjectName, keyword);
        }
        wrapper.orderByDesc(Project::getCreatedAt);
        return projectMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public ProjectApiKey generateProxyKey(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }

        String proxyKey = "acg-" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        ProjectApiKey apiKey = new ProjectApiKey();
        apiKey.setProjectId(projectId);
        apiKey.setProxyKey(proxyKey);
        apiKey.setQuotaTokens(-1L);
        apiKey.setQuotaAmount(java.math.BigDecimal.valueOf(-1));
        apiKey.setStatus(1);
        projectApiKeyMapper.insert(apiKey);
        return apiKey;
    }

    @Override
    public void revokeProxyKey(Long proxyKeyId) {
        ProjectApiKey apiKey = projectApiKeyMapper.selectById(proxyKeyId);
        if (apiKey == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "代理Key不存在");
        }
        apiKey.setStatus(0);
        projectApiKeyMapper.updateById(apiKey);
        // 清除缓存
        redisTemplate.delete(PROXY_KEY_CACHE + apiKey.getProxyKey());
    }

    @Override
    public ProjectApiKey getByProxyKey(String proxyKey) {
        // 先查缓存
        String cacheKey = PROXY_KEY_CACHE + proxyKey;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof ProjectApiKey) {
            return (ProjectApiKey) cached;
        }

        ProjectApiKey apiKey = projectApiKeyMapper.selectOne(
                new LambdaQueryWrapper<ProjectApiKey>().eq(ProjectApiKey::getProxyKey, proxyKey)
        );
        if (apiKey != null) {
            redisTemplate.opsForValue().set(cacheKey, apiKey, 5, TimeUnit.MINUTES);
        }
        return apiKey;
    }
}
