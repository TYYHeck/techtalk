package com.techtalk.controller;

import com.techtalk.common.Result;
import com.techtalk.dto.PostDTO;
import com.techtalk.security.CurrentUser;
import com.techtalk.service.PostService;
import com.techtalk.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /** 获取帖子列表（公开） */
    @GetMapping
    public Result<?> getPostList(@RequestParam(defaultValue = "1") Long page,
                                  @RequestParam(defaultValue = "10") Long size,
                                  @RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) String keyword,
                                  @AuthenticationPrincipal CurrentUser currentUser) {
        Long userId = currentUser != null ? currentUser.getUserId() : null;
        return postService.getPostList(page, size, categoryId, keyword, userId);
    }

    /** 获取帖子详情（公开） */
    @GetMapping("/{id}")
    public Result<PostVO> getPostById(@PathVariable Long id,
                                       @AuthenticationPrincipal CurrentUser currentUser) {
        Long userId = currentUser != null ? currentUser.getUserId() : null;
        return postService.getPostById(id, userId);
    }

    /** 发布帖子 */
    @PostMapping
    public Result<PostVO> createPost(@AuthenticationPrincipal CurrentUser currentUser,
                                      @Valid @RequestBody PostDTO dto) {
        return postService.createPost(currentUser.getUserId(), dto);
    }

    /** 编辑帖子 */
    @PutMapping("/{id}")
    public Result<PostVO> updatePost(@AuthenticationPrincipal CurrentUser currentUser,
                                      @PathVariable Long id,
                                      @Valid @RequestBody PostDTO dto) {
        return postService.updatePost(currentUser.getUserId(), id, dto);
    }

    /** 删除帖子 */
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@AuthenticationPrincipal CurrentUser currentUser,
                                    @PathVariable Long id) {
        return postService.deletePost(currentUser.getUserId(), id);
    }
}
