package com.techtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 关注/好友关系表
 * 状态说明：
 *   FOLLOWING - 单向关注（userId 关注了 friendId）
 *   当双方互相关注（A关注B且B关注A），即为"互关"（等同于原来好友）
 */
@Data
@TableName("tt_friend")
public class Friend {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（关注者） */
    private Long userId;

    /** 目标用户ID（被关注者） */
    private Long friendId;

    /** 状态：FOLLOWING/ACCEPTED/REJECTED（保留旧状态兼容） */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
