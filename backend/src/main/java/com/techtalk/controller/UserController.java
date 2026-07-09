package com.techtalk.controller;

import com.techtalk.common.Result;
import com.techtalk.dto.UserUpdateDTO;
import com.techtalk.entity.User;
import com.techtalk.security.CurrentUser;
import com.techtalk.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<User> getCurrentUser(@AuthenticationPrincipal CurrentUser currentUser) {
        return userService.getCurrentUser(currentUser.getUserId());
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@AuthenticationPrincipal CurrentUser currentUser,
                                       @Valid @RequestBody UserUpdateDTO dto) {
        return userService.updateProfile(currentUser.getUserId(), dto);
    }
}
