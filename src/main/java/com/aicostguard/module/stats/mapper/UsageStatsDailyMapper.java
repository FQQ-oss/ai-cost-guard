package com.aicostguard.module.stats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aicostguard.module.stats.entity.UsageStatsDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper
public interface UsageStatsDailyMapper extends BaseMapper<UsageStatsDaily> {

    @Select("SELECT COALESCE(SUM(total_requests), 0) FROM usage_stats_daily " +
            "WHERE project_id = #{projectId} AND stat_date BETWEEN #{start} AND #{end}")
    Long sumRequests(Long projectId, LocalDate start, LocalDate end);

    @Select("SELECT COALESCE(SUM(total_cost), 0) FROM usage_stats_daily " +
            "WHERE project_id = #{projectId} AND stat_date BETWEEN #{start} AND #{end}")
    BigDecimal sumCost(Long projectId, LocalDate start, LocalDate end);
}
