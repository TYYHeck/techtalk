package com.techtalk.controller;

import com.techtalk.common.Result;
import com.techtalk.dto.CommentDTO;
import com.techtalk.security.CurrentUser;
import com.techtalk.service.CommentService;
import com.techtalk.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 获取帖子评论（公开） */
    @GetMapping("/post/{postId}")
    public Result<List<CommentVO>> getCommentsByPost(@PathVariable Long postId,
                                                      @AuthenticationPrincipal CurrentUser currentUser) {
        Long userId = currentUser != null ? currentUser.getUserId() : null;
        return commentService.getCommentsByPostId(postId, userId);
    }

    /** 发表评论 */
    @PostMapping
    public Result<CommentVO> createComment(@AuthenticationPrincipal CurrentUser currentUser,
                                           @Valid @RequestBody CommentDTO dto) {
        return commentService.createComment(currentUser.getUserId(), dto);
    }

    /** 删除评论 */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@AuthenticationPrincipal CurrentUser currentUser,
                                       @PathVariable Long id) {
        return commentService.deleteComment(currentUser.getUserId(), id);
    }
}
