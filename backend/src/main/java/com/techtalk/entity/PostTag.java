package com.techtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 帖子-分类关联表（多对多）
 */
@Data
@TableName("tt_post_tag")
public class PostTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子ID */
    private Long postId;

    /** 分类ID */
    private Long categoryId;
}
