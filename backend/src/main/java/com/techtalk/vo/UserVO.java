package com.techtalk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息视图（脱敏）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private String role;
    private String status;
    private Integer postCount;
    private Integer likeCount;
    private LocalDateTime createdAt;
}
