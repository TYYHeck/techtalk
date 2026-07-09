package com.techtalk.service;

import com.techtalk.common.Result;
import com.techtalk.dto.CommentDTO;
import com.techtalk.vo.CommentVO;

import java.util.List;

/**
 * 评论服务
 */
public interface CommentService {

    Result<CommentVO> createComment(Long userId, CommentDTO dto);

    Result<Void> deleteComment(Long userId, Long commentId);

    Result<List<CommentVO>> getCommentsByPostId(Long postId, Long currentUserId);
}
