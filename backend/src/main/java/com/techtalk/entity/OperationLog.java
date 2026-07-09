package com.techtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表
 */
@Data
@TableName("tt_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人 ID */
    private Long userId;

    /** 操作人用户名 */
    private String username;

    /** 操作类型 */
    private String action;

    /** 操作目标 */
    private String target;

    /** 操作详情 */
    private String detail;

    /** 请求 IP */
    private String ip;

    /** 请求 URI */
    private String uri;

    /** 请求方法 */
    private String method;

    /** 耗时（ms） */
    private Long timeCost;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
