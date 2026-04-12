package com.aicostguard.module.stats.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UsageStatsQuery {
    private Long projectId;
    private Long teamId;
    private String provider;
    private String model;
    private LocalDate startDate;
    private LocalDate endDate;
}
