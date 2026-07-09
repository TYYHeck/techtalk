package com.techtalk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {

    private Long id;
    private String content;
    private Long postId;
    private Long parentId;
    private UserVO user;
    private UserVO replyToUser;
    private Integer likeCount;
    private Boolean isLiked;
    private List<CommentVO> children;

    private LocalDateTime createdAt;
}
