package com.techtalk.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.techtalk.common.PageResult;
import com.techtalk.common.Result;
import com.techtalk.entity.Comment;
import com.techtalk.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端 - 评论管理
 */
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentController {

    private final CommentMapper commentMapper;

    @GetMapping
    public Result<?> getCommentList(@RequestParam(defaultValue = "1") Long page,
                                     @RequestParam(defaultValue = "10") Long size,
                                     @RequestParam(required = false) String keyword) {
        Page<Comment> pageObj = new Page<>(page, size);
        Page<Map<String, Object>> result = commentMapper.selectAdminPage(pageObj, keyword);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentMapper.deleteById(id);
        return Result.ok();
    }
}
