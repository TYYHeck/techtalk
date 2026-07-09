package com.techtalk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techtalk.common.Result;
import com.techtalk.dto.LoginDTO;
import com.techtalk.dto.RegisterDTO;
import com.techtalk.dto.UserUpdateDTO;
import com.techtalk.entity.User;

import java.util.Map;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    Result<Map<String, Object>> register(RegisterDTO dto, String ip);

    Result<Map<String, Object>> login(LoginDTO dto, String ip);

    Result<Map<String, Object>> refreshToken(String refreshToken);

    Result<Void> logout(String token);

    Result<User> getCurrentUser(Long userId);

    Result<Void> updateProfile(Long userId, UserUpdateDTO dto);
}
