package com.techtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论表
 */
@Data
@TableName("tt_comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 内容 */
    private String content;

    /** 帖子 ID */
    private Long postId;

    /** 评论用户 ID */
    private Long userId;

    /** 父评论 ID（0 表示顶级评论） */
    private Long parentId;

    /** 回复目标用户 ID */
    private Long replyToUserId;

    /** 点赞数 */
    private Integer likeCount;

    /** 状态：PUBLISHED / DELETED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
