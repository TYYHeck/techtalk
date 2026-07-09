package com.techtalk.interceptor;

import com.techtalk.entity.OperationLog;
import com.techtalk.mapper.OperationLogMapper;
import com.techtalk.security.CurrentUser;
import com.techtalk.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 操作日志拦截器（记录非 GET 请求）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogInterceptor implements HandlerInterceptor {

    private final OperationLogMapper operationLogMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                              Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        // 只记录写操作
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        Long startTime = (Long) request.getAttribute("startTime");
        long timeCost = startTime != null ? System.currentTimeMillis() - startTime : 0;

        OperationLog logEntry = new OperationLog();
        logEntry.setUri(request.getRequestURI());
        logEntry.setMethod(method);
        logEntry.setIp(IpUtil.getClientIp(request));
        logEntry.setTimeCost(timeCost);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser user) {
            logEntry.setUserId(user.getUserId());
            logEntry.setUsername(user.getUsername());
        }

        logEntry.setAction(getAction(request.getRequestURI(), method));
        logEntry.setDetail("耗时 " + timeCost + "ms");

        try {
            operationLogMapper.insert(logEntry);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            log.error("操作日志记录失败", e);
        }
    }

    private String getAction(String uri, String method) {
        if (uri.contains("/auth/login")) return "用户登录";
        if (uri.contains("/auth/register")) return "用户注册";
        if (uri.contains("/posts") && "POST".equalsIgnoreCase(method)) return "发布帖子";
        if (uri.contains("/posts") && "PUT".equalsIgnoreCase(method)) return "编辑帖子";
        if (uri.contains("/posts") && "DELETE".equalsIgnoreCase(method)) return "删除帖子";
        if (uri.contains("/comments") && "POST".equalsIgnoreCase(method)) return "发表评论";
        if (uri.contains("/likes")) return "点赞操作";
        if (uri.contains("/favorites")) return "收藏操作";
        if (uri.contains("/admin")) return "管理操作";
        return method + " " + uri;
    }
}
