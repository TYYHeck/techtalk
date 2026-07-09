package com.techtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知表
 */
@Data
@TableName("tt_notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户 ID */
    private Long userId;

    /** 触发用户 ID */
    private Long fromUserId;

    /** 通知类型：LIKE / COMMENT / REPLY / SYSTEM */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 关联帖子 ID */
    private Long postId;

    /** 关联评论 ID */
    private Long commentId;

    /** 是否已读 */
    private Boolean isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
