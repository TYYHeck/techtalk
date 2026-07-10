package com.techtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("tt_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（BCrypt 加密） */
    private String password;

    /** 邮箱 */
    private String email;

    /** 头像 URL */
    private String avatar;

    /** 个性签名 */
    private String bio;

    /** 昵称 */
    private String nickname;

    /** 所在地 */
    private String location;

    /** 个人网站 */
    private String website;

    /** GitHub 主页 */
    private String github;

    /** 角色：USER / ADMIN */
    private String role;

    /** 状态：ACTIVE / BANNED */
    private String status;

    /** 发帖数 */
    private Integer postCount;

    /** 获赞数 */
    private Integer likeCount;

    /** 最后登录 IP */
    private String lastLoginIp;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
