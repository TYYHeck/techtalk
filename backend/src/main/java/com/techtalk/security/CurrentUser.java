package com.techtalk.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 当前登录用户信息
 */
@Data
@AllArgsConstructor
public class CurrentUser {
    private Long userId;
    private String username;
    private String role;

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
