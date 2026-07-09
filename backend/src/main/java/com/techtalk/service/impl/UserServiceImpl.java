package com.techtalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techtalk.common.BusinessException;
import com.techtalk.common.Result;
import com.techtalk.dto.LoginDTO;
import com.techtalk.dto.RegisterDTO;
import com.techtalk.dto.UserUpdateDTO;
import com.techtalk.entity.User;
import com.techtalk.mapper.UserMapper;
import com.techtalk.security.JwtTokenProvider;
import com.techtalk.service.UserService;
import com.techtalk.util.RedisUtil;
import com.techtalk.util.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;
    private final SensitiveWordFilter sensitiveWordFilter;

    @Override
    @Transactional
    public Result<Map<String, Object>> register(RegisterDTO dto, String ip) {
        // 1. 敏感词检查
        if (sensitiveWordFilter.containsSensitiveWord(dto.getUsername())) {
            return Result.badRequest("用户名包含敏感词");
        }

        // 2. 用户名唯一性
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            return Result.badRequest("用户名已存在");
        }

        // 3. 邮箱唯一性
        if (userMapper.selectByEmail(dto.getEmail()) != null) {
            return Result.badRequest("邮箱已被注册");
        }

        // 4. 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setAvatar("https://api.dicebear.com/7.x/initials/svg?seed=" + dto.getUsername());
        user.setPostCount(0);
        user.setLikeCount(0);
        user.setLastLoginIp(ip);
        user.setLastLoginTime(LocalDateTime.now());

        if (!save(user)) {
            throw new BusinessException("注册失败，请重试");
        }

        log.info("新用户注册: {} (ID: {})", user.getUsername(), user.getId());

        // 5. 生成 Token
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 6. 缓存 Token
        redisUtil.set("token:" + user.getId(), accessToken, 24, TimeUnit.HOURS);
        redisUtil.set("refresh:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        Map<String, Object> result = buildTokenResult(user, accessToken, refreshToken);
        return Result.ok("注册成功", result);
    }

    @Override
    public Result<Map<String, Object>> login(LoginDTO dto, String ip) {
        // 1. 查询用户
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            return Result.badRequest("用户名或密码错误");
        }

        // 2. 检查状态
        if ("BANNED".equals(user.getStatus())) {
            return Result.forbidden("账号已被封禁，请联系管理员");
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.badRequest("用户名或密码错误");
        }

        // 4. 更新登录信息
        user.setLastLoginIp(ip);
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);

        // 5. 生成 Token
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 6. 缓存 Token + 在线状态
        redisUtil.set("token:" + user.getId(), accessToken, 24, TimeUnit.HOURS);
        redisUtil.set("refresh:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);
        redisUtil.set("online:" + user.getId(), "1", 30, TimeUnit.MINUTES);

        log.info("用户登录: {} (IP: {})", user.getUsername(), ip);

        Map<String, Object> result = buildTokenResult(user, accessToken, refreshToken);
        return Result.ok("登录成功", result);
    }

    @Override
    public Result<Map<String, Object>> refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return Result.unauthorized("刷新令牌已过期，请重新登录");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String cachedRefresh = redisUtil.get("refresh:" + userId);

        if (!refreshToken.equals(cachedRefresh)) {
            return Result.unauthorized("刷新令牌无效");
        }

        User user = getById(userId);
        if (user == null) {
            return Result.unauthorized("用户不存在");
        }

        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        redisUtil.set("token:" + user.getId(), newAccessToken, 24, TimeUnit.HOURS);

        Map<String, Object> result = buildTokenResult(user, newAccessToken, refreshToken);
        return Result.ok(result);
    }

    @Override
    public Result<Void> logout(String token) {
        if (token != null) {
            // 将令牌加入黑名单
            redisUtil.sAdd("blacklist:token:" + token, "1");
            redisUtil.expire("blacklist:token:" + token, 24, TimeUnit.HOURS);

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            redisUtil.delete("token:" + userId);
            redisUtil.delete("refresh:" + userId);
            redisUtil.delete("online:" + userId);
        }
        return Result.ok();
    }

    @Override
    public Result<User> getCurrentUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        // 脱敏
        user.setPassword(null);
        return Result.ok(user);
    }

    @Override
    public Result<Void> updateProfile(Long userId, UserUpdateDTO dto) {
        User user = getById(userId);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        if (dto.getBio() != null) {
            if (sensitiveWordFilter.containsSensitiveWord(dto.getBio())) {
                return Result.badRequest("签名包含敏感词");
            }
            user.setBio(dto.getBio());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        updateById(user);
        return Result.ok();
    }

    private Map<String, Object> buildTokenResult(User user, String accessToken, String refreshToken) {
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", 86400);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("bio", user.getBio());
        userInfo.put("role", user.getRole());
        userInfo.put("postCount", user.getPostCount());
        userInfo.put("likeCount", user.getLikeCount());
        result.put("user", userInfo);

        return result;
    }
}
