package com.aicostguard.module.stats.task;

import com.aicostguard.module.stats.entity.UsageStatsDaily;
import com.aicostguard.module.stats.mapper.UsageStatsDailyMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsFlushTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UsageStatsDailyMapper statsDailyMapper;

    /**
     * 每5分钟将 Redis 实时统计刷入 MySQL
     */
    @Scheduled(fixedRate = 300000)
    public void flushStats() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String pattern = "stats:*:" + date + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            return;
        }

        log.info("开始刷新统计数据，共 {} 个 key", keys.size());

        for (String key : keys) {
            try {
                // key 格式: stats:{projectId}:{date}:{model}
                String[] parts = key.split(":");
                if (parts.length < 4) continue;

                Long projectId = Long.parseLong(parts[1]);
                String model = parts[3];

                Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);
                if (hash.isEmpty()) continue;

                long requests = toLong(hash.get("requests"));
                long promptTokens = toLong(hash.get("prompt_tokens"));
                long completionTokens = toLong(hash.get("completion_tokens"));
                double cost = toDouble(hash.get("cost"));
                long cacheHits = toLong(hash.get("cache_hits"));

                // 推断 provider
                String provider = inferProvider(model);
                LocalDate statDate = LocalDate.parse(date);

                // 查找已有记录
                UsageStatsDaily existing = statsDailyMapper.selectOne(
                        new LambdaQueryWrapper<UsageStatsDaily>()
                                .eq(UsageStatsDaily::getProjectId, projectId)
                                .eq(UsageStatsDaily::getProvider, provider)
                                .eq(UsageStatsDaily::getModel, model)
                                .eq(UsageStatsDaily::getStatDate, statDate)
                );

                if (existing != null) {
                    existing.setTotalRequests((int) requests);
                    existing.setTotalPromptTokens(promptTokens);
                    existing.setTotalCompletionTokens(completionTokens);
                    existing.setTotalCost(BigDecimal.valueOf(cost).setScale(6, RoundingMode.HALF_UP));
                    existing.setCacheHitCount((int) cacheHits);
                    statsDailyMapper.updateById(existing);
                } else {
                    UsageStatsDaily stats = new UsageStatsDaily();
                    stats.setProjectId(projectId);
                    stats.setTeamId(0L); // TODO: 从项目关联获取
                    stats.setProvider(provider);
                    stats.setModel(model);
                    stats.setStatDate(statDate);
                    stats.setTotalRequests((int) requests);
                    stats.setTotalPromptTokens(promptTokens);
                    stats.setTotalCompletionTokens(completionTokens);
                    stats.setTotalCost(BigDecimal.valueOf(cost).setScale(6, RoundingMode.HALF_UP));
                    stats.setCacheHitCount((int) cacheHits);
                    statsDailyMapper.insert(stats);
                }
            } catch (Exception e) {
                log.error("刷新统计失败: key={}", key, e);
            }
        }
        log.info("统计数据刷新完成");
    }

    private String inferProvider(String model) {
        if (model.startsWith("gpt") || model.startsWith("o1") || model.startsWith("o3")) return "openai";
        if (model.startsWith("claude")) return "claude";
        if (model.startsWith("deepseek")) return "deepseek";
        return "unknown";
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
