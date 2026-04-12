package com.aicostguard.infrastructure.filter;

import com.aicostguard.common.exception.BusinessException;
import com.aicostguard.common.result.ResultCode;
import com.aicostguard.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String REDIS_TOKEN_KEY = "auth:token:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(TOKEN_PREFIX)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String token = authHeader.substring(TOKEN_PREFIX.length());
        if (jwtUtils.isTokenExpired(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Token已过期");
        }

        Long userId = jwtUtils.getUserId(token);
        String username = jwtUtils.getUsername(token);

        // 检查 Redis 中是否存在（支持主动踢下线）
        String redisKey = REDIS_TOKEN_KEY + userId;
        Object storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请重新登录");
        }

        // 放入 request attribute，后续 Controller 可获取
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        return true;
    }
}
