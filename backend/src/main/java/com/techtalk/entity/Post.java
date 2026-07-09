package com.techtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子表
 */
@Data
@TableName("tt_post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 内容（富文本） */
    private String content;

    /** 纯文本摘要（用于搜索/列表展示） */
    private String summary;

    /** 分类 ID */
    private Long categoryId;

    /** 作者 ID */
    private Long userId;

    /** 浏览数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 收藏数 */
    private Integer favoriteCount;

    /** 是否置顶 */
    private Boolean isPinned;

    /** 是否精华 */
    private Boolean isFeatured;

    /** 状态：PUBLISHED / DRAFT / AUDITING / REJECTED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
