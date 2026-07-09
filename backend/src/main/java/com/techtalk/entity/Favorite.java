package com.techtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏记录表
 */
@Data
@TableName("tt_favorite")
public class Favorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 帖子 ID */
    private Long postId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
