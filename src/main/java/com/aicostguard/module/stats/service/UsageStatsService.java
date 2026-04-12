package com.aicostguard.module.stats.service;

import com.aicostguard.module.stats.dto.DailyStatsVO;
import com.aicostguard.module.stats.dto.UsageStatsQuery;
import com.aicostguard.module.stats.dto.UsageStatsSummary;

import java.util.List;
import java.util.Map;

public interface UsageStatsService {
    /**
     * 获取汇总统计
     */
    UsageStatsSummary getSummary(UsageStatsQuery query);

    /**
     * 获取每日趋势
     */
    List<DailyStatsVO> getDailyTrend(UsageStatsQuery query);

    /**
     * 获取按模型分组的统计
     */
    List<Map<String, Object>> getStatsByModel(UsageStatsQuery query);

    /**
     * 获取实时统计（从 Redis）
     */
    UsageStatsSummary getTodayRealtime(Long projectId);
}
