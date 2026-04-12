package com.aicostguard.module.proxy.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer maxTokens;
    private Boolean stream;

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
