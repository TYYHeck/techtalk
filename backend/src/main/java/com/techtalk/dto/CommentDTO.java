package com.techtalk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论请求
 */
@Data
public class CommentDTO {

    @NotNull(message = "帖子 ID 不能为空")
    private Long postId;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 2000, message = "评论长度 1-2000 位")
    private String content;

    /** 父评论 ID（回复时传，顶级评论传 0） */
    private Long parentId;

    /** 回复目标用户 ID */
    private Long replyToUserId;
}
