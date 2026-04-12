package com.aicostguard.module.proxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aicostguard.module.proxy.entity.RequestLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RequestLogMapper extends BaseMapper<RequestLog> {
}
