package com.techtalk.interceptor;

import com.techtalk.common.BusinessException;
import com.techtalk.util.IpUtil;
import com.techtalk.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 接口限流拦截器（基于 Redis 滑动窗口）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisUtil redisUtil;

    /** 每个 IP 每分钟最大请求数 */
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    /** 登录接口每分钟限制 */
    private static final int MAX_LOGIN_PER_MINUTE = 10;
    /** 注册接口每分钟限制 */
    private static final int MAX_REGISTER_PER_MINUTE = 3;
    /** 发帖接口每分钟限制 */
    private static final int MAX_POST_PER_MINUTE = 5;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                              Object handler) {
        String uri = request.getRequestURI();
        String ip = IpUtil.getClientIp(request);

        // 针对不同接口设置不同限流阈值
        int maxRequests = MAX_REQUESTS_PER_MINUTE;
        if (uri.contains("/auth/login")) {
            maxRequests = MAX_LOGIN_PER_MINUTE;
        } else if (uri.contains("/auth/register")) {
            maxRequests = MAX_REGISTER_PER_MINUTE;
        } else if (uri.contains("/posts") && "POST".equalsIgnoreCase(request.getMethod())) {
            maxRequests = MAX_POST_PER_MINUTE;
        }

        String redisKey = "rate_limit:" + ip + ":" + uri.replaceAll("/\\d+", "/*");
        Long count = redisUtil.increment(redisKey);
        if (count == 1) {
            redisUtil.expire(redisKey, 60, TimeUnit.SECONDS);
        }

        if (count != null && count > maxRequests) {
            log.warn("IP [{}] 请求过于频繁: {} ({}次/分钟)", ip, uri, count);
            throw new BusinessException(429, "操作过于频繁，请稍后再试");
        }

        return true;
    }
}
