package com.techtalk.interceptor;

import com.techtalk.common.BusinessException;
import com.techtalk.util.IpUtil;
import com.techtalk.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 接口限流拦截器（基于 Redis 计数器 + 用户级别双重限流）
 * <p>
 * 限流策略：
 * - 精确路径匹配，避免 contains() 绕过
 * - IP 级别限流（防止分布式攻击）
 * - 用户级别限流（防止换 IP 绕过，需登录态）
 * - 点赞、评论、发帖等敏感操作独立限流
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisUtil redisUtil;

    // ==================== IP 级别限流阈值 ====================
    /** 默认：每个 IP 每分钟最大请求数 */
    private static final int IP_DEFAULT_PER_MINUTE = 120;
    /** 登录：每个 IP 每分钟 */
    private static final int IP_LOGIN_PER_MINUTE = 10;
    /** 注册：每个 IP 每分钟 */
    private static final int IP_REGISTER_PER_MINUTE = 3;
    /** 发帖：每个 IP 每分钟 */
    private static final int IP_POST_PER_MINUTE = 5;
    /** 评论：每个 IP 每分钟 */
    private static final int IP_COMMENT_PER_MINUTE = 15;
    /** 点赞/取消点赞：每个 IP 每分钟 */
    private static final int IP_LIKE_PER_MINUTE = 30;

    // ==================== 用户级别限流阈值 ====================
    /** 发帖：每用户每分钟 */
    private static final int USER_POST_PER_MINUTE = 5;
    /** 评论：每用户每分钟 */
    private static final int USER_COMMENT_PER_MINUTE = 10;
    /** 点赞/取消点赞：每用户每分钟 */
    private static final int USER_LIKE_PER_MINUTE = 20;

    // ==================== 路径匹配常量 ====================
    private static final String PATH_LOGIN = "/api/auth/login";
    private static final String PATH_REGISTER = "/api/auth/register";
    private static final String PATH_SEND_CODE = "/api/auth/send-code";
    private static final String PATH_POSTS = "/api/posts";
    private static final String PATH_COMMENTS = "/api/comments";
    private static final String PATH_LIKES = "/api/likes";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                              Object handler) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String ip = IpUtil.getClientIp(request);

        // 1. 判断接口类型
        RateLimitConfig config = resolveConfig(uri, method);

        // 2. IP 级别限流
        String ipKey = "rate_limit:ip:" + ip + ":" + config.pathKey;
        checkAndIncrement(ipKey, config.ipLimit, "IP", ip, uri);

        // 3. 用户级别限流（从 JWT 中提取 userId）
        Long userId = extractUserId(request);
        if (userId != null && config.userLimit > 0) {
            String userKey = "rate_limit:user:" + userId + ":" + config.pathKey;
            checkAndIncrement(userKey, config.userLimit, "用户", userId.toString(), uri);
        }

        return true;
    }

    /**
     * 检查并递增计数器，超限则抛异常
     */
    private void checkAndIncrement(String key, int maxRequests, String label, String identifier, String uri) {
        Long count = redisUtil.increment(key);
        if (count != null && count == 1) {
            redisUtil.expire(key, 60, TimeUnit.SECONDS);
        }
        if (count != null && count > maxRequests) {
            log.warn("{} [{}] 请求过于频繁: {} ({}次/分钟)", label, identifier, uri, count);
            throw new BusinessException(429, "操作过于频繁，请稍后再试");
        }
    }

    /**
     * 根据 URI 和方法解析限流配置（精确匹配）
     */
    private RateLimitConfig resolveConfig(String uri, String method) {
        String normalizedUri = normalizeUri(uri);

        // 登录/注册/验证码
        if (normalizedUri.equals(PATH_LOGIN) || normalizedUri.equals(PATH_SEND_CODE)) {
            return new RateLimitConfig("auth:login", IP_LOGIN_PER_MINUTE, 0);
        }
        if (normalizedUri.equals(PATH_REGISTER)) {
            return new RateLimitConfig("auth:register", IP_REGISTER_PER_MINUTE, 0);
        }

        // 发帖
        if (normalizedUri.startsWith(PATH_POSTS) && "POST".equalsIgnoreCase(method)) {
            return new RateLimitConfig("post:create", IP_POST_PER_MINUTE, USER_POST_PER_MINUTE);
        }

        // 评论
        if (normalizedUri.startsWith(PATH_COMMENTS) && "POST".equalsIgnoreCase(method)) {
            return new RateLimitConfig("comment:create", IP_COMMENT_PER_MINUTE, USER_COMMENT_PER_MINUTE);
        }

        // 点赞/取消点赞
        if (normalizedUri.startsWith(PATH_LIKES)) {
            return new RateLimitConfig("like:toggle", IP_LIKE_PER_MINUTE, USER_LIKE_PER_MINUTE);
        }

        // 默认
        return new RateLimitConfig("default", IP_DEFAULT_PER_MINUTE, 0);
    }

    /**
     * 标准化 URI：去掉数字 ID 和尾部斜杠
     */
    private String normalizeUri(String uri) {
        if (uri == null) return "";
        uri = uri.replaceAll("/\\d+", "/*");
        if (uri.endsWith("/") && uri.length() > 1) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    /**
     * 从请求中提取用户 ID（从 JWT Token 解析）
     */
    private Long extractUserId(HttpServletRequest request) {
        try {
            // 从 Authorization header 获取 token
            String bearerToken = request.getHeader("Authorization");
            if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
                return null;
            }
            String token = bearerToken.substring(7);
            // 从 SecurityContext 获取认证信息
            Object principal = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication() != null
                    ? org.springframework.security.core.context.SecurityContextHolder
                            .getContext().getAuthentication().getPrincipal()
                    : null;
            if (principal instanceof com.techtalk.security.CurrentUser currentUser) {
                return currentUser.getUserId();
            }
        } catch (Exception e) {
            // 无法提取用户 ID，跳过用户级别限流
        }
        return null;
    }

    /**
     * 限流配置内部类
     */
    private static class RateLimitConfig {
        final String pathKey;
        final int ipLimit;
        final int userLimit;

        RateLimitConfig(String pathKey, int ipLimit, int userLimit) {
            this.pathKey = pathKey;
            this.ipLimit = ipLimit;
            this.userLimit = userLimit;
        }
    }
}
