package com.aicostguard.module.stats.controller;

import com.aicostguard.common.result.Result;
import com.aicostguard.module.stats.dto.DailyStatsVO;
import com.aicostguard.module.stats.dto.UsageStatsQuery;
import com.aicostguard.module.stats.dto.UsageStatsSummary;
import com.aicostguard.module.stats.service.UsageStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class UsageStatsController {

    private final UsageStatsService usageStatsService;

    @GetMapping("/summary")
    public Result<UsageStatsSummary> getSummary(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        UsageStatsQuery query = new UsageStatsQuery();
        query.setProjectId(projectId);
        query.setTeamId(teamId);
        query.setProvider(provider);
        query.setModel(model);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        return Result.success(usageStatsService.getSummary(query));
    }

    @GetMapping("/trend")
    public Result<List<DailyStatsVO>> getDailyTrend(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        UsageStatsQuery query = new UsageStatsQuery();
        query.setProjectId(projectId);
        query.setTeamId(teamId);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        return Result.success(usageStatsService.getDailyTrend(query));
    }

    @GetMapping("/by-model")
    public Result<List<Map<String, Object>>> getByModel(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        UsageStatsQuery query = new UsageStatsQuery();
        query.setProjectId(projectId);
        query.setTeamId(teamId);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        return Result.success(usageStatsService.getStatsByModel(query));
    }

    @GetMapping("/realtime/{projectId}")
    public Result<UsageStatsSummary> getRealtimeStats(@PathVariable Long projectId) {
        return Result.success(usageStatsService.getTodayRealtime(projectId));
    }
}
