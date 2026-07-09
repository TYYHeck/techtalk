package com.techtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞记录表
 */
@Data
@TableName("tt_like_record")
public class LikeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 目标类型：POST / COMMENT */
    private String targetType;

    /** 目标 ID */
    private Long targetId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
