import request from './request'

// 认证
export const login = data => request.post('/auth/login', data)
export const register = data => request.post('/auth/register', data)
export const logout = () => request.post('/auth/logout')

// 用户管理
export const getUsers = params => request.get('/system/users', { params })
export const createUser = data => request.post('/system/users', data)
export const updateUser = (id, data) => request.put(`/system/users/${id}`, data)
export const deleteUser = id => request.delete(`/system/users/${id}`)

// 团队管理
export const getTeams = params => request.get('/system/teams', { params })
export const createTeam = data => request.post('/system/teams', data)
export const myTeams = () => request.get('/system/teams/my')

// API Key 池
export const getKeyPool = params => request.get('/apikey/pool', { params })
export const addKey = data => request.post('/apikey/pool', data)
export const deleteKey = id => request.delete(`/apikey/pool/${id}`)
export const toggleKeyStatus = (id, status) => request.put(`/apikey/pool/${id}/status`, { status })

// 项目管理
export const getProjects = params => request.get('/apikey/projects', { params })
export const createProject = data => request.post('/apikey/projects', data)
export const deleteProject = id => request.delete(`/apikey/projects/${id}`)
export const generateProxyKey = projectId => request.post(`/apikey/projects/${projectId}/generate-key`)
export const revokeProxyKey = id => request.delete(`/apikey/projects/proxy-keys/${id}`)

// 模型定价
export const getPricing = params => request.get('/apikey/pricing', { params })
export const addPricing = data => request.post('/apikey/pricing', data)
export const updatePricing = (id, data) => request.put(`/apikey/pricing/${id}`, data)

// 路由规则
export const getRouteRules = params => request.get('/router/rules', { params })
export const createRouteRule = data => request.post('/router/rules', data)
export const deleteRouteRule = id => request.delete(`/router/rules/${id}`)

// 用量统计
export const getStatsSummary = params => request.get('/stats/summary', { params })
export const getStatsTrend = params => request.get('/stats/trend', { params })
export const getStatsByModel = params => request.get('/stats/by-model', { params })
export const getRealtimeStats = projectId => request.get(`/stats/realtime/${projectId}`)

// 缓存配置
export const getCacheConfig = projectId => request.get(`/cache/config/${projectId}`)
export const saveCacheConfig = data => request.post('/cache/config', data)

// 告警
export const getAlertRules = params => request.get('/alert/rules', { params })
export const createAlertRule = data => request.post('/alert/rules', data)
export const deleteAlertRule = id => request.delete(`/alert/rules/${id}`)
export const getAlertHistory = params => request.get('/alert/history', { params })

// 审计日志
export const getAuditLogs = params => request.get('/audit/logs', { params })
