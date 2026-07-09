package com.techtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类表
 */
@Data
@TableName("tt_category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名 */
    private String name;

    /** 描述 */
    private String description;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sortOrder;

    /** 帖子数 */
    private Integer postCount;

    /** 状态：ACTIVE / DISABLED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
