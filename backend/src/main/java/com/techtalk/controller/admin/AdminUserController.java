package com.techtalk.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.techtalk.common.PageResult;
import com.techtalk.common.Result;
import com.techtalk.entity.User;
import com.techtalk.mapper.UserMapper;
import com.techtalk.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 管理端 - 用户管理
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserMapper userMapper;
    private final RedisUtil redisUtil;

    @GetMapping
    public Result<PageResult<User>> getUserList(@RequestParam(defaultValue = "1") Long page,
                                                 @RequestParam(defaultValue = "10") Long size,
                                                 @RequestParam(required = false) String keyword) {
        Page<User> pageObj = new Page<>(page, size);
        Page<User> result = userMapper.selectPageWithKeyword(pageObj, keyword);
        // 脱敏
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            return Result.badRequest("不能封禁管理员");
        }
        user.setStatus(body.get("status"));
        userMapper.updateById(user);

        // 封禁后清除 Token 强制下线
        if ("BANNED".equals(body.get("status"))) {
            redisUtil.delete("token:" + id);
            redisUtil.delete("refresh:" + id);
            redisUtil.delete("online:" + id);
        }

        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            return Result.badRequest("不能删除管理员");
        }
        userMapper.deleteById(id);
        redisUtil.delete("token:" + id);
        redisUtil.delete("online:" + id);
        return Result.ok();
    }
}
