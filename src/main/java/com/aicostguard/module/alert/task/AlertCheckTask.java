package com.aicostguard.module.alert.task;

import com.aicostguard.module.alert.entity.AlertHistory;
import com.aicostguard.module.alert.entity.AlertRule;
import com.aicostguard.module.alert.mapper.AlertHistoryMapper;
import com.aicostguard.module.alert.mapper.AlertRuleMapper;
import com.aicostguard.module.alert.service.AlertNotifyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertCheckTask {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertHistoryMapper alertHistoryMapper;
    private final AlertNotifyService alertNotifyService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 每分钟检测告警规则
     */
    @Scheduled(fixedRate = 60000)
    public void checkAlerts() {
        List<AlertRule> rules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>().eq(AlertRule::getStatus, 1)
        );

        for (AlertRule rule : rules) {
            try {
                boolean triggered = evaluate(rule);
                if (triggered) {
                    String content = buildAlertContent(rule);

                    // 记录告警历史
                    AlertHistory history = new AlertHistory();
                    history.setRuleId(rule.getId());
                    history.setAlertContent(content);
                    history.setNotifyStatus(1);
                    alertHistoryMapper.insert(history);

                    // 发送通知
                    if (rule.getNotifyChannels() != null) {
                        alertNotifyService.notify(rule.getNotifyChannels(), content);
                    }
                }
            } catch (Exception e) {
                log.error("告警检测异常: ruleId={}", rule.getId(), e);
            }
        }
    }

    private boolean evaluate(AlertRule rule) {
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return switch (rule.getAlertType()) {
            case "budget" -> {
                if (rule.getProjectId() == null || rule.getThreshold() == null) yield false;
                String amountKey = "budget:amount:" + rule.getProjectId() + ":" + month;
                Object usedObj = redisTemplate.opsForValue().get(amountKey);
                if (usedObj == null) yield false;
                BigDecimal used = new BigDecimal(usedObj.toString());
                yield used.compareTo(rule.getThreshold()) >= 0;
            }
            case "spike" -> {
                // 用量突增检测：对比今日与昨日的请求数
                // 简化实现：检查今日总 token 是否超过阈值
                if (rule.getProjectId() == null) yield false;
                String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String pattern = "stats:" + rule.getProjectId() + ":" + date + ":*";
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys == null || keys.isEmpty()) yield false;
                long totalTokens = 0;
                for (String key : keys) {
                    Object tokens = redisTemplate.opsForHash().get(key, "prompt_tokens");
                    Object compTokens = redisTemplate.opsForHash().get(key, "completion_tokens");
                    totalTokens += (tokens != null ? Long.parseLong(tokens.toString()) : 0);
                    totalTokens += (compTokens != null ? Long.parseLong(compTokens.toString()) : 0);
                }
                yield rule.getThreshold() != null && totalTokens > rule.getThreshold().longValue();
            }
            default -> false;
        };
    }

    private String buildAlertContent(AlertRule rule) {
        return String.format("[AI Cost Guard 告警] 规则: %s, 类型: %s, 项目ID: %s",
                rule.getRuleName(), rule.getAlertType(),
                rule.getProjectId() != null ? rule.getProjectId() : "全局");
    }
}
