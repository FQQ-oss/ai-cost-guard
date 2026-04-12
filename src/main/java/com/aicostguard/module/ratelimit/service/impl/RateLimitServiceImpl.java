package com.aicostguard.module.ratelimit.service.impl;

import com.aicostguard.module.ratelimit.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LUA_SCRIPT = """
            local key = KEYS[1]
            local max_requests = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local window_start = now - window * 1000

            redis.call('ZREMRANGEBYSCORE', key, '-inf', window_start)
            local current = redis.call('ZCARD', key)

            if current < max_requests then
                redis.call('ZADD', key, now, now .. '-' .. math.random(1000000))
                redis.call('EXPIRE', key, window)
                return 1
            else
                return 0
            end
            """;

    @Override
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        String redisKey = "ratelimit:" + key;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(LUA_SCRIPT);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(redisKey),
                maxRequests,
                windowSeconds,
                System.currentTimeMillis()
        );
        return result != null && result == 1;
    }
}
