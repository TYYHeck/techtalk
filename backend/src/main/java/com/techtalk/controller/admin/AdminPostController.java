package com.techtalk.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.techtalk.common.PageResult;
import com.techtalk.common.Result;
import com.techtalk.entity.Post;
import com.techtalk.mapper.PostMapper;
import com.techtalk.security.CurrentUser;
import com.techtalk.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 帖子管理
 */
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPostController {

    private final PostMapper postMapper;
    private final RedisUtil redisUtil;

    @GetMapping
    public Result<?> getPostList(@RequestParam(defaultValue = "1") Long page,
                                  @RequestParam(defaultValue = "10") Long size,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String keyword) {
        Page<Post> pageObj = new Page<>(page, size);
        Page<Post> result = postMapper.selectAdminPage(pageObj, status, keyword);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updatePostStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.notFound("帖子不存在");
        }
        post.setStatus(body.get("status"));
        postMapper.updateById(post);
        redisUtil.delete("post:detail:" + id);
        redisUtil.deleteByPattern("post:list:*");
        return Result.ok();
    }

    @PutMapping("/{id}/pin")
    public Result<Void> togglePin(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.notFound("帖子不存在");
        }
        post.setIsPinned(!post.getIsPinned());
        postMapper.updateById(post);
        redisUtil.deleteByPattern("post:list:*");
        return Result.ok();
    }

    @PutMapping("/{id}/feature")
    public Result<Void> toggleFeature(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.notFound("帖子不存在");
        }
        post.setIsFeatured(!post.getIsFeatured());
        postMapper.updateById(post);
        redisUtil.delete("post:detail:" + id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postMapper.deleteById(id);
        redisUtil.delete("post:detail:" + id);
        redisUtil.deleteByPattern("post:list:*");
        return Result.ok();
    }
}
