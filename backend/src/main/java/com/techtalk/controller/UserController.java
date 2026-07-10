package com.techtalk.controller;

import com.techtalk.common.Result;
import com.techtalk.dto.UserUpdateDTO;
import com.techtalk.entity.User;
import com.techtalk.security.CurrentUser;
import com.techtalk.service.FriendService;
import com.techtalk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FriendService friendService;

    @GetMapping("/me")
    public Result<User> getCurrentUser(@AuthenticationPrincipal CurrentUser currentUser) {
        return userService.getCurrentUser(currentUser.getUserId());
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@AuthenticationPrincipal CurrentUser currentUser,
                                       @Valid @RequestBody UserUpdateDTO dto) {
        return userService.updateProfile(currentUser.getUserId(), dto);
    }

    /** 查看用户主页（公开） */
    @GetMapping("/{userId}")
    public Result<Map<String, Object>> getUserProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal(expression = "userId") Long currentUserId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("avatar", user.getAvatar());
        profile.put("bio", user.getBio());
        profile.put("postCount", user.getPostCount());
        profile.put("likeCount", user.getLikeCount());
        profile.put("role", user.getRole());
        profile.put("createdAt", user.getCreatedAt());

        // 检查是否为好友
        if (currentUserId != null && !currentUserId.equals(userId)) {
            profile.put("isFriend", friendService.isFriend(currentUserId, userId));
            // 检查是否已发送好友请求
            profile.put("isSelf", false);
        } else if (currentUserId != null && currentUserId.equals(userId)) {
            profile.put("isSelf", true);
        }
        return Result.ok(profile);
    }

    /** 更新密码 */
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.badRequest("参数不能为空");
        }
        return userService.updatePassword(currentUser.getUserId(), oldPassword, newPassword);
    }

    /** 更新邮箱 */
    @PutMapping("/email")
    public Result<Void> updateEmail(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestBody Map<String, String> body) {
        String newEmail = body.get("email");
        String code = body.get("code");
        if (newEmail == null || code == null) {
            return Result.badRequest("参数不能为空");
        }
        return userService.updateEmail(currentUser.getUserId(), newEmail, code);
    }
}

