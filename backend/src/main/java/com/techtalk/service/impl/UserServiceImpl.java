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
    private final com.techtalk.service.EmailService emailService;

    /** 同一 IP 每天最大注册次数 */
    private static final int MAX_REGISTER_PER_IP_PER_DAY = 5;

    @Override
    @Transactional
    public Result<Map<String, Object>> register(RegisterDTO dto, String ip) {
        // 0. 二次密码校验（DTO 层已有 @AssertTrue，此处双重保障）
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return Result.badRequest("两次输入的密码不一致");
        }

        // 0.5 IP 注册频率限制（每日上限）
        String ipLimitKey = "register:ip:limit:" + ip;
        String ipLimitCount = redisUtil.get(ipLimitKey);
        if (ipLimitCount != null && Integer.parseInt(ipLimitCount) >= MAX_REGISTER_PER_IP_PER_DAY) {
            log.warn("IP [{}] 今日注册次数已达上限", ip);
            return Result.badRequest("今日注册次数已达上限，请明天再试");
        }

        // 0.6 验证邮箱验证码
        if (!emailService.verifyCode(dto.getEmail(), dto.getEmailCode(), "register", true)) {
            return Result.badRequest("邮箱验证码错误或已过期");
        }

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

        // 增加 IP 注册计数
        long secondsUntilTomorrow = getSecondsUntilTomorrow();
        if (ipLimitCount == null) {
            redisUtil.set(ipLimitKey, "1", secondsUntilTomorrow, TimeUnit.SECONDS);
        } else {
            redisUtil.increment(ipLimitKey);
        }

        log.info("新用户注册: {} (ID: {}, IP: {})", user.getUsername(), user.getId(), ip);

        // 5. 生成 Token
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 6. 缓存 Token
        redisUtil.set("token:" + user.getId(), accessToken, 24, TimeUnit.HOURS);
        redisUtil.set("refresh:" + user.getId(), refreshToken, 7, TimeUnit.DAYS);

        Map<String, Object> result = buildTokenResult(user, accessToken, refreshToken);
        return Result.ok("注册成功", result);
    }

    /**
     * 计算距离明天 0 点还有多少秒
     */
    private long getSecondsUntilTomorrow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return java.time.Duration.between(now, tomorrow).getSeconds();
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
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getLocation() != null) {
            user.setLocation(dto.getLocation());
        }
        if (dto.getWebsite() != null) {
            user.setWebsite(dto.getWebsite());
        }
        if (dto.getGithub() != null) {
            user.setGithub(dto.getGithub());
        }
        updateById(user);
        return Result.ok();
    }

    @Override
    public Result<Void> updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.badRequest("原密码错误");
        }
        if (newPassword.length() < 6) {
            return Result.badRequest("新密码长度不能少于6位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
        return Result.ok();
    }

    @Override
    public Result<Void> updateEmail(Long userId, String newEmail, String code) {
        User user = getById(userId);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        if (!emailService.verifyCode(newEmail, code, "resetPassword", true)) {
            return Result.badRequest("邮箱验证码错误或已过期");
        }
        if (userMapper.selectByEmail(newEmail) != null) {
            return Result.badRequest("该邮箱已被使用");
        }
        user.setEmail(newEmail);
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
        userInfo.put("nickname", user.getNickname());
        userInfo.put("role", user.getRole());
        userInfo.put("postCount", user.getPostCount());
        userInfo.put("likeCount", user.getLikeCount());
        result.put("user", userInfo);

        return result;
    }
}
