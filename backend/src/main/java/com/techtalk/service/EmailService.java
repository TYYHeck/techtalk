package com.techtalk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final com.techtalk.util.RedisUtil redisUtil;

    @Value("${spring.mail.username}")
    private String from;

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int SEND_INTERVAL_SECONDS = 60;

    /**
     * 发送验证码到指定邮箱
     * @param to 收件人邮箱
     * @param purpose 用途（register / resetPassword）
     */
    public boolean sendVerificationCode(String to, String purpose) {
        // 发送频率限制：同一邮箱同一用途 60 秒内只能发送一次
        String intervalKey = "email:interval:" + purpose + ":" + to;
        if (redisUtil.hasKey(intervalKey)) {
            log.warn("邮箱 {} 发送验证码过于频繁（用途：{}）", to, purpose);
            return false;
        }

        // 生成 6 位数字验证码
        String code = generateCode();

        // 存到 Redis，5 分钟过期
        String codeKey = "email:code:" + purpose + ":" + to;
        redisUtil.set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 设置发送间隔标记
        redisUtil.set(intervalKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(getSubject(purpose));
            message.setText(buildEmailContent(code, purpose));
            mailSender.send(message);

            log.info("验证码已发送至 {}（用途：{}，有效期 {} 分钟）", to, purpose, CODE_EXPIRE_MINUTES);
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败：{}", e.getMessage());
            // 发送失败，清除 Redis 中的验证码
            redisUtil.delete(codeKey);
            redisUtil.delete(intervalKey);
            return false;
        }
    }

    /**
     * 验证邮箱验证码
     * @param email 邮箱
     * @param code 用户输入的验证码
     * @param purpose 用途
     * @param deleteAfterVerify 验证后是否删除（true=一次性，false=可复用）
     */
    public boolean verifyCode(String email, String code, String purpose, boolean deleteAfterVerify) {
        String codeKey = "email:code:" + purpose + ":" + email;
        String cachedCode = redisUtil.get(codeKey);

        if (cachedCode == null) {
            return false; // 验证码不存在或已过期
        }

        boolean matched = cachedCode.equals(code);

        if (matched && deleteAfterVerify) {
            redisUtil.delete(codeKey); // 验证成功，删除验证码（一次性使用）
        }

        return matched;
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String getSubject(String purpose) {
        return switch (purpose) {
            case "register" -> "【TechTalk】邮箱验证码 - 注册账号";
            case "resetPassword" -> "【TechTalk】邮箱验证码 - 重置密码";
            default -> "【TechTalk】邮箱验证码";
        };
    }

    private String buildEmailContent(String code, String purpose) {
        String action = switch (purpose) {
            case "register" -> "注册 TechTalk 账号";
            case "resetPassword" -> "重置您的 TechTalk 账号密码";
            default -> "完成您的操作";
        };

        return """
                您好！

                您正在%s，验证码如下：

                    %s

                验证码 %d 分钟内有效，请勿泄露给他人。

                如果这不是您本人的操作，请忽略此邮件。

                —— TechTalk 团队
                """.formatted(action, code, CODE_EXPIRE_MINUTES);
    }
}
