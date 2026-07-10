package com.techtalk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息表
 */
@Data
@TableName("tt_chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID（senderId_receiverId 排序后拼接） */
    private String conversationId;

    /** 发送者ID */
    private Long senderId;

    /** 接收者ID */
    private Long receiverId;

    /** 消息内容 */
    private String content;

    /** 是否已读 */
    private Boolean isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
