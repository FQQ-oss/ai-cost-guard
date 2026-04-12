package com.aicostguard.module.stats.service.impl;

import com.aicostguard.module.stats.dto.DailyStatsVO;
import com.aicostguard.module.stats.dto.UsageStatsQuery;
import com.aicostguard.module.stats.dto.UsageStatsSummary;
import com.aicostguard.module.stats.entity.UsageStatsDaily;
import com.aicostguard.module.stats.mapper.UsageStatsDailyMapper;
import com.aicostguard.module.stats.service.UsageStatsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsageStatsServiceImpl implements UsageStatsService {

    private final UsageStatsDailyMapper statsDailyMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public UsageStatsSummary getSummary(UsageStatsQuery query) {
        LambdaQueryWrapper<UsageStatsDaily> wrapper = buildWrapper(query);
        List<UsageStatsDaily> list = statsDailyMapper.selectList(wrapper);

        long totalRequests = list.stream().mapToLong(s -> s.getTotalRequests() != null ? s.getTotalRequests() : 0).sum();
        long totalPromptTokens = list.stream().mapToLong(s -> s.getTotalPromptTokens() != null ? s.getTotalPromptTokens() : 0).sum();
        long totalCompletionTokens = list.stream().mapToLong(s -> s.getTotalCompletionTokens() != null ? s.getTotalCompletionTokens() : 0).sum();
        BigDecimal totalCost = list.stream()
                .map(s -> s.getTotalCost() != null ? s.getTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long cacheHits = list.stream().mapToLong(s -> s.getCacheHitCount() != null ? s.getCacheHitCount() : 0).sum();

        double cacheHitRate = totalRequests > 0 ? (double) cacheHits / totalRequests * 100 : 0;

        return UsageStatsSummary.builder()
                .totalRequests(totalRequests)
                .totalPromptTokens(totalPromptTokens)
                .totalCompletionTokens(totalCompletionTokens)
                .totalTokens(totalPromptTokens + totalCompletionTokens)
                .totalCost(totalCost)
                .cacheHitCount(cacheHits)
                .cacheHitRate(Math.round(cacheHitRate * 100.0) / 100.0)
                .build();
    }

    @Override
    public List<DailyStatsVO> getDailyTrend(UsageStatsQuery query) {
        LambdaQueryWrapper<UsageStatsDaily> wrapper = buildWrapper(query);
        wrapper.orderByAsc(UsageStatsDaily::getStatDate);
        List<UsageStatsDaily> list = statsDailyMapper.selectList(wrapper);

        // 按日期聚合
        Map<LocalDate, List<UsageStatsDaily>> grouped = list.stream()
                .collect(Collectors.groupingBy(UsageStatsDaily::getStatDate));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    DailyStatsVO vo = new DailyStatsVO();
                    vo.setDate(entry.getKey());
                    List<UsageStatsDaily> dayStats = entry.getValue();
                    vo.setRequests(dayStats.stream().mapToLong(s -> s.getTotalRequests() != null ? s.getTotalRequests() : 0).sum());
                    vo.setPromptTokens(dayStats.stream().mapToLong(s -> s.getTotalPromptTokens() != null ? s.getTotalPromptTokens() : 0).sum());
                    vo.setCompletionTokens(dayStats.stream().mapToLong(s -> s.getTotalCompletionTokens() != null ? s.getTotalCompletionTokens() : 0).sum());
                    vo.setCost(dayStats.stream()
                            .map(s -> s.getTotalCost() != null ? s.getTotalCost() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    vo.setCacheHits(dayStats.stream().mapToLong(s -> s.getCacheHitCount() != null ? s.getCacheHitCount() : 0).sum());
                    return vo;
                })
                .toList();
    }

    @Override
    public List<Map<String, Object>> getStatsByModel(UsageStatsQuery query) {
        LambdaQueryWrapper<UsageStatsDaily> wrapper = buildWrapper(query);
        List<UsageStatsDaily> list = statsDailyMapper.selectList(wrapper);

        // 按 provider:model 聚合
        Map<String, List<UsageStatsDaily>> grouped = list.stream()
                .collect(Collectors.groupingBy(s -> s.getProvider() + ":" + s.getModel()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split(":", 2);
                    List<UsageStatsDaily> stats = entry.getValue();
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("provider", parts[0]);
                    map.put("model", parts.length > 1 ? parts[1] : "");
                    map.put("totalRequests", stats.stream().mapToLong(s -> s.getTotalRequests() != null ? s.getTotalRequests() : 0).sum());
                    map.put("totalTokens", stats.stream().mapToLong(s ->
                            (s.getTotalPromptTokens() != null ? s.getTotalPromptTokens() : 0) +
                            (s.getTotalCompletionTokens() != null ? s.getTotalCompletionTokens() : 0)).sum());
                    map.put("totalCost", stats.stream()
                            .map(s -> s.getTotalCost() != null ? s.getTotalCost() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    return map;
                })
                .toList();
    }

    @Override
    public UsageStatsSummary getTodayRealtime(Long projectId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String pattern = "stats:" + projectId + ":" + date + ":*";

        Set<String> keys = redisTemplate.keys(pattern);
        long totalRequests = 0, totalPrompt = 0, totalCompletion = 0, cacheHits = 0;
        double totalCost = 0;

        if (keys != null) {
            for (String key : keys) {
                Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);
                totalRequests += toLong(hash.get("requests"));
                totalPrompt += toLong(hash.get("prompt_tokens"));
                totalCompletion += toLong(hash.get("completion_tokens"));
                totalCost += toDouble(hash.get("cost"));
                cacheHits += toLong(hash.get("cache_hits"));
            }
        }

        double cacheHitRate = totalRequests > 0 ? (double) cacheHits / totalRequests * 100 : 0;

        return UsageStatsSummary.builder()
                .totalRequests(totalRequests)
                .totalPromptTokens(totalPrompt)
                .totalCompletionTokens(totalCompletion)
                .totalTokens(totalPrompt + totalCompletion)
                .totalCost(BigDecimal.valueOf(totalCost).setScale(6, RoundingMode.HALF_UP))
                .cacheHitCount(cacheHits)
                .cacheHitRate(Math.round(cacheHitRate * 100.0) / 100.0)
                .build();
    }

    private LambdaQueryWrapper<UsageStatsDaily> buildWrapper(UsageStatsQuery query) {
        LambdaQueryWrapper<UsageStatsDaily> wrapper = new LambdaQueryWrapper<>();
        if (query.getProjectId() != null) {
            wrapper.eq(UsageStatsDaily::getProjectId, query.getProjectId());
        }
        if (query.getTeamId() != null) {
            wrapper.eq(UsageStatsDaily::getTeamId, query.getTeamId());
        }
        if (query.getProvider() != null) {
            wrapper.eq(UsageStatsDaily::getProvider, query.getProvider());
        }
        if (query.getModel() != null) {
            wrapper.eq(UsageStatsDaily::getModel, query.getModel());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(UsageStatsDaily::getStatDate, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(UsageStatsDaily::getStatDate, query.getEndDate());
        }
        return wrapper;
    }

    private long toLong(Object obj) {
        if (obj == null) return 0;
        return Long.parseLong(obj.toString());
    }

    private double toDouble(Object obj) {
        if (obj == null) return 0;
        return Double.parseDouble(obj.toString());
    }
}
