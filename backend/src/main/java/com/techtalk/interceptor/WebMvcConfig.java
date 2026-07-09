package com.techtalk.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final LogInterceptor logInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 限流拦截器（所有接口）
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/doc.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**");

        // 日志拦截器（所有接口）
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/api/**");
    }
}
