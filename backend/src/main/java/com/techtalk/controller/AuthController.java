package com.techtalk.controller;

import com.techtalk.common.Result;
import com.techtalk.dto.LoginDTO;
import com.techtalk.dto.RegisterDTO;
import com.techtalk.dto.SendEmailCodeDTO;
import com.techtalk.security.JwtTokenProvider;
import com.techtalk.service.EmailService;
import com.techtalk.service.UserService;
import com.techtalk.util.IpUtil;
import com.techtalk.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;
    private final EmailService emailService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto,
                                                  HttpServletRequest request) {
        return userService.register(dto, IpUtil.getClientIp(request));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto,
                                              HttpServletRequest request) {
        return userService.login(dto, IpUtil.getClientIp(request));
    }

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return userService.refreshToken(refreshToken);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        return userService.logout(token);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-code")
    public Result<Void> sendEmailCode(@Valid @RequestBody SendEmailCodeDTO dto) {
        boolean sent = emailService.sendVerificationCode(dto.getEmail(), dto.getPurpose());
        if (sent) {
            return Result.ok();
        } else {
            return Result.badRequest("验证码发送过于频繁，请 60 秒后再试");
        }
    }
}
