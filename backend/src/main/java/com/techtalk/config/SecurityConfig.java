package com.techtalk.config;

import com.techtalk.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 白名单（无需认证） */
    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/posts/public/**",
            "/api/categories/public/**",
            "/api/comments/public/**",
            "/doc.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/favicon.ico",
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（API 服务不需要）
            .csrf(AbstractHttpConfigurer::disable)
            // 无状态会话
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 请求授权
            .authorizeHttpRequests(auth -> auth
                // 白名单放行
                .requestMatchers(WHITE_LIST).permitAll()
                // 公开 GET 接口
                .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/{id:\\d+}").permitAll()
                // 管理端接口需 ADMIN 角色
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 其余接口需认证
                .anyRequest().authenticated()
            )
            // JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 异常处理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
                })
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
