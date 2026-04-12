package com.aicostguard.module.alert.service.impl;

import com.aicostguard.module.alert.service.AlertNotifyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertNotifyServiceImpl implements AlertNotifyService {

    private final ObjectMapper objectMapper;

    @Override
    public void notify(String channels, String content) {
        try {
            List<String> channelList = objectMapper.readValue(channels, new TypeReference<List<String>>() {});
            for (String channel : channelList) {
                switch (channel) {
                    case "email" -> sendEmail(content);
                    case "webhook" -> sendWebhook(content);
                    default -> log.warn("未知通知渠道: {}", channel);
                }
            }
        } catch (Exception e) {
            log.error("解析通知渠道失败", e);
        }
    }

    private void sendEmail(String content) {
        // TODO: 集成 Spring Mail
        log.info("[邮件通知] {}", content);
    }

    private void sendWebhook(String content) {
        // TODO: 调用企微/钉钉 Webhook
        log.info("[Webhook通知] {}", content);
    }
}
